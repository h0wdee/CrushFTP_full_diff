/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.Base64;
import com.crushftp.client.File_S;
import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.gui.LOC;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.SharedSession;
import crushftp.handlers.SharedSessionReplicated;
import crushftp.handlers.SyncTools;
import crushftp.handlers.UserTools;
import crushftp.server.AdminControls;
import crushftp.server.As2Msg;
import crushftp.server.QuickConnect;
import crushftp.server.ServerSessionAJAX;
import crushftp.server.ServerStatus;
import crushftp.server.VFS;
import crushftp.server.daemon.DMZTunnelClient;
import crushftp.server.daemon.DMZTunnelClient5;
import crushftp.server.daemon.GenericServer;
import crushftp.server.daemon.ServerBeat;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class DMZServerCommon
extends GenericServer {
    public static Properties dmzInstances = new Properties();
    public static Properties dmzResponses = new Properties();
    DMZServerCommon thisObj = this;
    Exception error = null;
    Socket read_sock = null;
    Socket write_sock = null;
    Properties socks_in_out = new Properties();
    Vector responseQueue = new Vector();
    int messages_received = 0;
    int messages_sent = 0;
    String singleton_id = Common.makeBoundary();
    File_S prefs_file = null;
    public static Properties last_prefs_time = new Properties();
    public static transient Object stop_send_prefs = new Object();
    public String last_write_info = "";
    long last_ping = System.currentTimeMillis();
    long last_pong = System.currentTimeMillis();
    Socket logging_socket = null;
    ObjectInputStream logging_socket_ois = null;
    public static int MAX_DMZ_SOCKET_IDLE_TIME = 10000;
    DMZTunnelClient dmz_tunnel_client_d = null;
    DMZTunnelClient dmz_tunnel_client_s = null;
    DMZTunnelClient5 dmz_tunnel_client_d5 = null;
    long last_logging_socket_time = 0L;
    String dmz_related_internal_settings_hash = "";
    Thread current_thread = null;

    public DMZServerCommon(Properties server_item) {
        super(server_item);
        try {
            com.crushftp.client.Common.System2.put("crushftp.dmz.factory", ServerStatus.thisObj.common_code.getSSLContext("builtin", null, "crushftp", "crushftp", "TLS", false, true).getSocketFactory());
        }
        catch (Exception e) {
            Log.log("DMZ", 0, e);
        }
    }

    public static void sendCommand(String instance_name, Properties data, String type, String id) {
        DMZServerCommon.sendCommand(instance_name, data, null, type, id);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void sendCommand(String instance_name, Properties data, String site, String type, String id) {
        block16: {
            DMZServerCommon dmz = (DMZServerCommon)dmzInstances.get(instance_name);
            try {
                if (dmz.write_sock == null) break block16;
                Properties p = new Properties();
                p.put("type", type.toUpperCase());
                p.put("data", data);
                if (site != null) {
                    p.put("site", site);
                }
                p.put("id", id);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos);
                out.reset();
                Object object = SharedSession.sessionLock;
                synchronized (object) {
                    out.writeObject(p);
                    out.close();
                }
                byte[] b = baos.toByteArray();
                int loops = 0;
                while (loops++ < 5) {
                    try {
                        long start = System.currentTimeMillis();
                        long waited = 0L;
                        Socket socket = dmz.write_sock;
                        synchronized (socket) {
                            waited = System.currentTimeMillis() - start;
                            start = System.currentTimeMillis();
                            Properties in_out = (Properties)dmz.socks_in_out.get(dmz.write_sock);
                            DataInputStream din = (DataInputStream)in_out.get("in");
                            DataOutputStream dout = (DataOutputStream)in_out.get("out");
                            dmz.write_sock.setSoTimeout(20000);
                            dout.writeInt(b.length);
                            dout.write(b);
                            dout.flush();
                            int i = din.readInt();
                            if (i != b.length) {
                                throw new Exception("Invalid response received from DMZ send:" + i);
                            }
                            ++dmz.messages_sent;
                            dmz.last_write_info = "[Waited " + waited + "ms for send socket, send took " + (System.currentTimeMillis() - start) + "ms for " + b.length + "bytes for " + p.getProperty("type") + " at " + new Date() + "]";
                        }
                        Log.log("DMZ", 2, "WROTE:" + instance_name + ":" + p.getProperty("type") + ":" + p.getProperty("id"));
                        break;
                    }
                    catch (Exception e) {
                        block17: {
                            if (loops >= 4) {
                                throw e;
                            }
                            Log.log("DMZ", 0, e);
                            Log.log("DMZ", 0, "Restarting DMZ write sock due to error:" + e);
                            try {
                                DMZServerCommon.closeInOutSockRef(dmz.socks_in_out, dmz.write_sock);
                                dmz.getNewWriteSock();
                            }
                            catch (Exception e1) {
                                Log.log("DMZ", 0, e1);
                                if (loops < 4) break block17;
                                throw e1;
                            }
                        }
                        Thread.sleep(2000L);
                    }
                }
            }
            catch (Exception e) {
                dmz.error = e;
            }
        }
    }

    public void getNewWriteSock() throws Exception {
    }

    public void getNewReadSock() throws Exception {
    }

    public void start_connection() throws Exception {
    }

    public String getDmzNameHostPort() {
        boolean tunneled = this.server_item.getProperty("tunneled", "false").equals("true") || this.server_item.getProperty("dmz_version", "").equals("4") || this.server_item.getProperty("dmz_version", "").equals("5");
        return String.valueOf(this.server_item.getProperty("server_item_name")) + ":" + this.server_item.getProperty("ip").trim() + ":" + Integer.parseInt(this.server_item.getProperty("port")) + ":" + tunneled;
    }

    public void reset_dmz_connection(String dmz_name_host_port) {
        Vector v = (Vector)com.crushftp.client.Common.System2.get("crushftp.dmz.hosts");
        if (v != null) {
            v.remove(dmz_name_host_port);
        }
        this.socket_created = false;
        if (this.dmz_tunnel_client_d != null) {
            this.dmz_tunnel_client_d.close();
            this.dmz_tunnel_client_d = null;
        }
        if (this.dmz_tunnel_client_s != null) {
            this.dmz_tunnel_client_s.close();
            this.dmz_tunnel_client_s = null;
        }
        if (this.dmz_tunnel_client_d5 != null) {
            this.dmz_tunnel_client_d5.close();
            this.dmz_tunnel_client_d5 = null;
        }
        try {
            this.server_sock.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        DMZTunnelClient.reset_sockets();
        this.updateStatus();
        if (this.restart) {
            try {
                Thread.sleep(1000L);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (ServerStatus.thisObj.main_servers.indexOf(this) >= 0) {
                ServerStatus.thisObj.start_this_server(ServerStatus.thisObj.main_servers.indexOf(this));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        this.current_thread = Thread.currentThread();
        this.init();
        String dmz_name_host_port = "";
        try {
            if (ServerStatus.SG("never_ban").indexOf(this.listen_ip) < 0) {
                if (!ServerStatus.SG("never_ban").trim().equals("*")) {
                    if (!ServerStatus.SG("never_ban").equals("disabled")) {
                        ServerStatus.server_settings.put("never_ban", String.valueOf(ServerStatus.SG("never_ban")) + "," + this.listen_ip);
                    }
                }
            }
            if (ServerStatus.siIG("enterprise_level") <= 0) {
                this.busyMessage = "DMZ only valid for Enterprise licenses.";
                throw new Exception(this.busyMessage);
            }
            if (com.crushftp.client.Common.dmz_mode) {
                this.busyMessage = "DMZ port cannot operate on a DMZ server, only on an internal server.";
                throw new Exception(this.busyMessage);
            }
            this.getSocket();
            this.server_sock.close();
            int x22 = 0;
            while (x22 < UserTools.anyPassTokens.size()) {
                SharedSessionReplicated.send("", "anyPassToken", "anyPassToken", UserTools.anyPassTokens.elementAt(x22).toString());
                ++x22;
            }
            this.busyMessage = "Finding DMZ...";
            this.start_connection();
            dmzInstances.put(this.server_item.getProperty("server_item_name"), this);
            this.busyMessage = "Starting DMZ...";
            this.startSocketConnectors();
            this.dmz_related_internal_settings_hash = this.generateDMZRelatedInternalSettingsHash();
            this.load_and_send_prefs(false);
            Worker.startWorker(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    last_prefs_time.put(DMZServerCommon.this.server_item.getProperty("server_item_name"), String.valueOf(DMZServerCommon.this.prefs_file.lastModified()));
                    StringBuffer die_now2 = DMZServerCommon.this.die_now;
                    while (die_now2.length() == 0) {
                        Object object = stop_send_prefs;
                        synchronized (object) {
                            try {
                                String temp_update_hash = DMZServerCommon.this.generateDMZRelatedInternalSettingsHash();
                                if (!DMZServerCommon.this.dmz_related_internal_settings_hash.equals(temp_update_hash)) {
                                    DMZServerCommon.this.dmz_related_internal_settings_hash = temp_update_hash;
                                    Thread.sleep(1000L);
                                    Log.log("SERVER", 0, "DMZ related settings change were detected. Save and re-sending to DMZ");
                                    DMZServerCommon.this.load_and_send_prefs(false);
                                }
                                long old_time = Long.parseLong(last_prefs_time.getProperty(DMZServerCommon.this.server_item.getProperty("server_item_name", "")));
                                if (DMZServerCommon.this.prefs_file.lastModified() != old_time) {
                                    Thread.sleep(1000L);
                                    Log.log("SERVER", 0, "DMZ prefs file change detected, re-sending to DMZ:" + DMZServerCommon.this.prefs_file + " Current time:" + DMZServerCommon.this.prefs_file.lastModified() + " Versus old recorded time:" + old_time + " Diff:" + (DMZServerCommon.this.prefs_file.lastModified() - old_time) + "ms");
                                    DMZServerCommon.this.load_and_send_prefs(false);
                                }
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                        }
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
            }, "DMZ prefs.XML file update monitor:" + this.prefs_file);
            if (this.error != null) {
                throw this.error;
            }
            this.busyMessage = "";
            Properties x22 = dmzInstances;
            synchronized (x22) {
                Vector<String> v = (Vector<String>)com.crushftp.client.Common.System2.get("crushftp.dmz.hosts");
                if (v == null) {
                    v = new Vector<String>();
                }
                com.crushftp.client.Common.System2.put("crushftp.dmz.hosts", v);
                dmz_name_host_port = this.getDmzNameHostPort();
                v.addElement(dmz_name_host_port);
            }
            long lastToken = 0L;
            StringBuffer die_now2 = this.die_now;
            this.last_ping = System.currentTimeMillis();
            this.last_pong = System.currentTimeMillis();
            while (this.socket_created && die_now2.length() == 0) {
                if (System.currentTimeMillis() - lastToken > 30000L) {
                    this.sendToken();
                    lastToken = System.currentTimeMillis();
                }
                if (this.error != null) {
                    throw this.error;
                }
                Thread.sleep(500L);
                if (System.currentTimeMillis() - this.last_ping > 10000L) {
                    Properties ping = new Properties();
                    ping.put("id", Common.makeBoundary());
                    ping.put("time", String.valueOf(System.currentTimeMillis()));
                    this.last_ping = System.currentTimeMillis();
                    DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), ping, "PUT:PING", ping.getProperty("id"));
                }
                if (Math.abs(this.last_ping - this.last_pong) <= ServerStatus.LG("dmz_pong_timeout") * 1000L) continue;
                Log.log("DMZ", 0, "No pong reply to ping command after " + ServerStatus.IG("dmz_pong_timeout") + " seconds. Restarting DMZ port.  last_ping=" + new Date(this.last_ping) + " last_pong=" + new Date(this.last_pong));
                if (Log.log("DMZ", 1, "")) {
                    com.crushftp.client.Common.sockLog(this.read_sock, com.crushftp.client.Common.dumpStack("1:No pong reply to ping command after " + ServerStatus.IG("dmz_pong_timeout") + " seconds. Restarting DMZ port.  last_ping=" + new Date(this.last_ping) + " last_pong=" + new Date(this.last_pong)));
                }
                throw new Exception("No pong reply to ping command after " + ServerStatus.IG("dmz_pong_timeout") + " seconds. Restarting DMZ port:" + this.listen_ip + ":" + this.listen_port + "  " + this.last_write_info + " responseQueue_size=" + this.responseQueue.size());
            }
            this.restart = true;
        }
        catch (InterruptedException e) {
            Log.log("DMZ", 0, e);
            this.die_now.append(System.currentTimeMillis());
        }
        catch (ConnectException e) {
            Log.log("DMZ", 3, e);
            this.restart = true;
            this.die_now.append(System.currentTimeMillis());
        }
        catch (SocketException e) {
            Log.log("DMZ", 2, e);
            this.restart = true;
            this.die_now.append(System.currentTimeMillis());
        }
        catch (Exception e) {
            Log.log("DMZ", 0, e);
            this.restart = true;
            this.die_now.append(System.currentTimeMillis());
        }
        this.reset_dmz_connection(dmz_name_host_port);
    }

    public static Properties getResponse(String id, int timeout) throws Exception {
        long start = System.currentTimeMillis();
        while (!dmzResponses.containsKey(id) && start > System.currentTimeMillis() - (long)(1000 * timeout)) {
            Thread.sleep(1L);
        }
        if (!dmzResponses.containsKey(id)) {
            return null;
        }
        return (Properties)dmzResponses.remove(id);
    }

    public void load_and_send_prefs(boolean needSave) throws Exception {
        this.prefs_file = new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs_" + this.server_item.getProperty("server_item_name") + ".XML");
        ServerStatus.siPUT("currentFileDate_" + this.server_item.getProperty("server_item_name"), String.valueOf(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs_" + this.server_item.getProperty("server_item_name") + ".XML").lastModified()));
        Properties instance_server_settings = ServerStatus.thisObj.prefsProvider.loadPrefs(this.server_item.getProperty("server_item_name"));
        instance_server_settings.put("registration_name", ServerStatus.SG("registration_name"));
        instance_server_settings.put("registration_email", ServerStatus.SG("registration_email"));
        instance_server_settings.put("registration_code", ServerStatus.SG("registration_code"));
        instance_server_settings.put("tunnels_dmz", ServerStatus.VG("tunnels"));
        instance_server_settings.put("miniURLs_dmz", ServerStatus.VG("miniURLs"));
        instance_server_settings.put("v8_beta", "true");
        ServerStatus.thisObj.common_code.set_defaults(ServerStatus.thisObj.default_settings);
        Enumeration<?> the_list = ServerStatus.thisObj.default_settings.propertyNames();
        while (the_list.hasMoreElements()) {
            Object cur = the_list.nextElement();
            if (instance_server_settings.get(cur.toString()) != null) continue;
            instance_server_settings.put(cur.toString(), ServerStatus.thisObj.default_settings.get(cur.toString()));
            if (ServerStatus.thisObj.default_settings.get(cur.toString()) instanceof Vector && ((Vector)ServerStatus.thisObj.default_settings.get(cur.toString())).size() == 0 || ServerStatus.thisObj.default_settings.get(cur.toString()) instanceof Properties && ((Properties)ServerStatus.thisObj.default_settings.get(cur.toString())).size() == 0) continue;
            needSave = true;
        }
        if (needSave) {
            Common.writeXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs_" + this.server_item.getProperty("server_item_name") + ".XML", (Object)instance_server_settings, "server_prefs");
        }
        last_prefs_time.put(this.server_item.getProperty("server_item_name"), String.valueOf(this.prefs_file.lastModified()));
        DMZServerCommon.sendFileToMemory(instance_server_settings.getProperty("cert_path", ""), this.server_item.getProperty("server_item_name"));
        Vector instance_servers = (Vector)instance_server_settings.get("server_list");
        int x = 0;
        while (x < instance_servers.size()) {
            DMZServerCommon.sendFileToMemory(((Properties)instance_servers.elementAt(x)).getProperty("customKeystore", ""), this.server_item.getProperty("server_item_name"));
            DMZServerCommon.sendFileToMemory(String.valueOf(((Properties)instance_servers.elementAt(x)).getProperty("customKeystore", "")) + "_trust", this.server_item.getProperty("server_item_name"));
            DMZServerCommon.sendFileToMemory(((Properties)instance_servers.elementAt(x)).getProperty("ssh_rsa_key", ""), this.server_item.getProperty("server_item_name"));
            DMZServerCommon.sendFileToMemory(((Properties)instance_servers.elementAt(x)).getProperty("ssh_dsa_key", ""), this.server_item.getProperty("server_item_name"));
            ++x;
        }
        DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), instance_server_settings, "PUT:SERVER_SETTINGS", "");
    }

    private String generateDMZRelatedInternalSettingsHash() throws Exception {
        StringBuffer log = new StringBuffer();
        log.append(ServerStatus.SG("registration_name"));
        log.append(";");
        log.append(ServerStatus.SG("registration_email"));
        log.append(";");
        log.append(ServerStatus.SG("registration_code"));
        Common.updateObjectLog(ServerStatus.VG("tunnels"), new Vector(), log, true);
        log.append(";");
        log.append(this.server_item.getProperty("cert_path"));
        log.append(";");
        Common.updateObjectLog(ServerStatus.VG("miniURLs"), new Vector(), log, true);
        log.append(";");
        log.append(ServerStatus.SG("cert_path"));
        Vector instance_servers = ServerStatus.VG("server_list");
        int x = 0;
        while (x < instance_servers.size()) {
            log.append(((Properties)instance_servers.elementAt(x)).getProperty("customKeystore", ""));
            log.append(";");
            log.append(String.valueOf(((Properties)instance_servers.elementAt(x)).getProperty("ssh_rsa_key", "")) + "_trust");
            log.append(";");
            log.append(String.valueOf(((Properties)instance_servers.elementAt(x)).getProperty("ssh_dsa_key", "")) + "_trust");
            log.append(";");
            ++x;
        }
        return log.toString();
    }

    public void sendToken() {
        Properties system_prop = new Properties();
        system_prop.put("key", "crushftp.proxy.anyPassToken");
        system_prop.put("val", UserTools.anyPassTokens.elementAt(0).toString());
        DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), system_prop, "PUT:SYSTEM.PROPERTIES", "");
        SharedSessionReplicated.send("", "anyPassToken", "anyPassToken", UserTools.anyPassTokens.elementAt(0).toString());
    }

    public void processResponse(Properties p) throws Exception {
        Log.log("DMZ", 2, "READ:" + this.server_item.getProperty("server_item_name") + ":" + p.getProperty("type") + ":" + p.getProperty("id"));
        if (p.getProperty("type").equalsIgnoreCase("RESPONSE")) {
            p.put("received", String.valueOf(System.currentTimeMillis()));
            dmzResponses.put(p.getProperty("id"), p);
        } else if (p.getProperty("type").equalsIgnoreCase("GET:USER_SSH_KEYS")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Properties user;
                    String linkedServer;
                    Vector public_keys;
                    block17: {
                        block16: {
                            Properties server_item_temp = null;
                            int x = 0;
                            while (x < ServerStatus.VG("server_list").size()) {
                                Properties si = (Properties)ServerStatus.VG("server_list").elementAt(x);
                                if (si.getProperty("serverType").startsWith("HTTP") && si.getProperty("port").equals(String.valueOf(Integer.parseInt(p2.getProperty("preferred_port", "0"))))) {
                                    Log.log("SERVER", 2, "GET:SOCKET:Prefered port found:" + p2.getProperty("preferred_port", "0") + ":" + si.getProperty("linkedServer"));
                                    server_item_temp = si;
                                    break;
                                }
                                ++x;
                            }
                            if (server_item_temp == null) {
                                Log.log("SERVER", 2, "GET:SOCKET:Prefered port not found...finding first HTTP(s) item to use..." + p2);
                                x = 0;
                                while (x < ServerStatus.VG("server_list").size()) {
                                    server_item_temp = (Properties)ServerStatus.VG("server_list").elementAt(x);
                                    if (server_item_temp.getProperty("serverType").equals("HTTP") || server_item_temp.getProperty("serverType").equals("HTTPS")) break;
                                    ++x;
                                }
                            }
                            if (server_item_temp == null) {
                                server_item_temp = DMZServerCommon.this.server_item;
                            }
                            public_keys = null;
                            linkedServer = p2.getProperty("linkedServer", server_item_temp.getProperty("linkedServer", "MainUsers"));
                            user = UserTools.ut.getUser(linkedServer, p2.getProperty("username"), true);
                            if (user == null && System.getProperty("crushftp.webapplication.enabled", "false").equals("false")) break block16;
                            if (!ServerStatus.BG("always_validate_plugins_for_dmz_lookup")) break block17;
                        }
                        Log.log("SERVER", 2, "GET:SOCKET:Attempting simulated login via plugins...");
                        SessionCrush tempSession = new SessionCrush(null, 1, "127.0.0.1", 0, "0.0.0.0", linkedServer, DMZServerCommon.this.server_item);
                        try {
                            tempSession.user_info.put("no_log_invalid_password", "true");
                            tempSession.verify_user(p2.getProperty("username"), p2.getProperty("password", String.valueOf(System.currentTimeMillis())), true, false);
                            if (tempSession.user != null) {
                                user = tempSession.user;
                                if (tempSession.user.getProperty("ssh_public_keys").equals("")) {
                                    Properties pp = new Properties();
                                    pp.put("user", user);
                                    pp.put("username", p2.getProperty("username"));
                                    pp.put("password", "");
                                    pp.put("anyPass", "true");
                                    pp.put("publickey_lookup", "true");
                                    pp = tempSession.runPlugin("login", pp);
                                    user.getProperty("ssh_public_keys");
                                }
                            }
                        }
                        catch (Exception e) {
                            Log.log("DMZ", 1, e);
                        }
                    }
                    try {
                        if (user != null) {
                            public_keys = UserTools.buildPublicKeys(p2.getProperty("username"), user, linkedServer);
                        }
                    }
                    catch (IOException e) {
                        Log.log("DMZ", 1, e);
                    }
                    if (user != null) {
                        user.remove("filePublicEncryptionKey");
                        user.remove("fileEncryptionKey");
                        user.remove("fileDecryptionKey");
                        p2.put("public_keys", public_keys);
                        p2.put("user", user);
                    }
                    DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:USER")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        VFS uVFS;
                        Properties user;
                        Vector public_keys;
                        Properties server_item_temp;
                        block29: {
                            block28: {
                                Log.log("HTTP_SERVER", 2, "DMZ GET USER :" + p2.getProperty("username"));
                                server_item_temp = null;
                                int x = 0;
                                while (x < ServerStatus.VG("server_list").size()) {
                                    Properties si = (Properties)ServerStatus.VG("server_list").elementAt(x);
                                    if (si.getProperty("serverType").startsWith("HTTP") && si.getProperty("port").equals(String.valueOf(Integer.parseInt(p2.getProperty("preferred_port", "0"))))) {
                                        Log.log("SERVER", 2, "GET:SOCKET:Prefered port found:" + p2.getProperty("preferred_port", "0") + ":" + si.getProperty("linkedServer"));
                                        server_item_temp = si;
                                        break;
                                    }
                                    ++x;
                                }
                                if (server_item_temp == null) {
                                    Log.log("SERVER", 2, "GET:SOCKET:Prefered port not found...finding first HTTP(s) item to use..." + p2);
                                    x = 0;
                                    while (x < ServerStatus.VG("server_list").size()) {
                                        server_item_temp = (Properties)ServerStatus.VG("server_list").elementAt(x);
                                        if (server_item_temp.getProperty("serverType").equals("HTTP") || server_item_temp.getProperty("serverType").equals("HTTPS")) break;
                                        ++x;
                                    }
                                }
                                if (server_item_temp == null) {
                                    server_item_temp = DMZServerCommon.this.server_item;
                                }
                                public_keys = null;
                                user = UserTools.ut.getUser(server_item_temp.getProperty("linkedServer", ""), p2.getProperty("username"), true);
                                uVFS = null;
                                try {
                                    uVFS = UserTools.ut.getVFS(server_item_temp.getProperty("linkedServer", ""), p2.getProperty("username"));
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 0, e);
                                    user = null;
                                }
                                if (user == null && System.getProperty("crushftp.webapplication.enabled", "false").equals("false")) break block28;
                                if (!ServerStatus.BG("always_validate_plugins_for_dmz_lookup")) break block29;
                            }
                            Log.log("SERVER", 2, "GET:SOCKET:Attempting simulated login via plugins...");
                            SessionCrush tempSession = new SessionCrush(null, 1, "127.0.0.1", 0, "0.0.0.0", server_item_temp.getProperty("linkedServer", ""), server_item_temp);
                            try {
                                tempSession.user_info.put("request", p2);
                                tempSession.user_info.put("no_log_invalid_password", "true");
                                boolean otp_validation = false;
                                if (user != null) {
                                    boolean bl = otp_validation = user.getProperty("otp_auth", "").equals("true") && p2.getProperty("password", "").indexOf(":") >= 0;
                                }
                                if (!otp_validation) {
                                    tempSession.verify_user(p2.getProperty("username"), p2.getProperty("password", String.valueOf(System.currentTimeMillis())), false, false);
                                }
                                if (!otp_validation) {
                                    user = tempSession.user;
                                }
                                if (!otp_validation) {
                                    uVFS = tempSession.uVFS;
                                }
                            }
                            catch (Exception e) {
                                Log.log("DMZ", 1, e);
                            }
                        }
                        if (user != null) {
                            public_keys = UserTools.buildPublicKeys(p2.getProperty("username"), user, server_item_temp.getProperty("linkedServer", ""));
                        }
                        if (user != null) {
                            DMZServerCommon.sendFileToMemory(user.getProperty("as2EncryptKeystorePath", ""), DMZServerCommon.this.server_item.getProperty("server_item_name", ""));
                            DMZServerCommon.sendFileToMemory(user.getProperty("as2SignKeystorePath", ""), DMZServerCommon.this.server_item.getProperty("server_item_name", ""));
                            UserTools.setupVFSLinking(server_item_temp.getProperty("linkedServer", ""), p2.getProperty("username"), uVFS, user);
                            user.remove("filePublicEncryptionKey");
                            user.remove("fileEncryptionKey");
                            user.remove("fileDecryptionKey");
                            if (user.getProperty("otp_auth", "").equals("true")) {
                                Properties otp_tokens = (Properties)ServerStatus.thisObj.server_info.get("otp_tokens");
                                String username = p2.getProperty("username", "");
                                if (ServerStatus.BG("username_uppercase")) {
                                    username = username.toUpperCase();
                                }
                                if (ServerStatus.BG("lowercase_usernames")) {
                                    username = username.toLowerCase();
                                }
                                Log.log("LOGIN", 1, "DMZ CHALLENGE_OTP : User: " + username + " with Ip: " + p2.getProperty("user_ip", ""));
                                if (otp_tokens != null && otp_tokens.containsKey(String.valueOf(username) + (p2.getProperty("user_ip", "").equals("") ? "127.0.0.1" : p2.getProperty("user_ip"))) && p2.getProperty("password", "").indexOf(":") >= 0) {
                                    Properties token = (Properties)otp_tokens.get(String.valueOf(username) + (p2.getProperty("user_ip", "").equals("") ? "127.0.0.1" : p2.getProperty("user_ip")));
                                    String password = p2.getProperty("password");
                                    if (password.indexOf(":") >= 0) {
                                        password = password.substring(password.lastIndexOf(":") + 1);
                                    }
                                    if (!user.getProperty("twofactor_secret", "").equals("")) {
                                        password = "TOTP:" + ServerStatus.thisObj.common_code.decode_pass(user.getProperty("twofactor_secret"));
                                    }
                                    if (token.getProperty("token", "").equalsIgnoreCase(password)) {
                                        Log.log("LOGIN", 1, "DMZ CHALLENGE_OTP : OTP token is valid.");
                                        user.put("otp_valid", "true");
                                    } else {
                                        user.put("otp_valid", "false");
                                        Log.log("LOGIN", 1, "DMZ CHALLENGE_OTP : OTP invalid.");
                                    }
                                }
                            }
                            p2.put("public_keys", public_keys);
                            p2.put("user", user);
                            p2.put("vfs", uVFS.homes);
                        }
                        DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name", ""), p2, "RESPONSE", p2.getProperty("id"));
                    }
                    catch (Exception e) {
                        Log.log("DMZ", 0, e);
                    }
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:RESET_TOKEN")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        Properties server_item2 = (Properties)DMZServerCommon.this.server_item.clone();
                        if (!p2.getProperty("internal_port", "").equals("")) {
                            Vector v = ServerStatus.VG("server_list");
                            int x = 0;
                            while (x < v.size()) {
                                Properties server_item3 = (Properties)v.elementAt(x);
                                if ((server_item3.getProperty("serverType").equalsIgnoreCase("HTTP") || server_item3.getProperty("serverType").equalsIgnoreCase("HTTPS")) && server_item3.getProperty("port").equals(p2.getProperty("internal_port"))) {
                                    server_item2 = server_item3;
                                    break;
                                }
                                ++x;
                            }
                        } else if (DMZServerCommon.this.server_item.getProperty("linkedServer", "").equals("@AutoHostHttp")) {
                            try {
                                VRL vrl = new VRL(p2.getProperty("currentURL"));
                                Vector v = ServerStatus.VG("login_page_list");
                                int x = 0;
                                while (x < v.size()) {
                                    Properties p = (Properties)v.elementAt(x);
                                    if (com.crushftp.client.Common.do_search(p.getProperty("domain"), vrl.getHost(), false, 0)) {
                                        String stem = p.getProperty("page").substring(0, p.getProperty("page").lastIndexOf("."));
                                        server_item2.put("linkedServer", stem);
                                    }
                                    ++x;
                                }
                            }
                            catch (Exception e) {
                                Log.log("HTTP_SERVER", 1, e);
                            }
                        }
                        String responseText = ServerSessionAJAX.doResetToken(p2.getProperty("reset_username_email"), p2.getProperty("currentURL"), server_item2.getProperty("linkedServer", ""), p2.getProperty("reset_token"), DMZServerCommon.this.singleton_id.equals(p2.getProperty("singleton_id", DMZServerCommon.this.singleton_id)), p2.getProperty("lang", "en"));
                        p2.put("responseText", responseText);
                        DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                    }
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:RECAPTCHA_RESPONSE")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        p2.put("responseText", ServerSessionAJAX.getRecaptchaResponse(p2.getProperty("recapcha_info")));
                        DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                    }
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:GENERATE_TOKEN")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        Properties request = (Properties)p2.remove("request");
                        request.put("method", "generateToken");
                        request.put("pluginName", "CrushSSO");
                        request.put("pluginSubItem", request.getProperty("pluginSubItem", ""));
                        p2.put("responseText", AdminControls.pluginMethodCall(Common.urlDecodePost(request), p2.getProperty("site", "")));
                        DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                        if (SharedSessionReplicated.send_queues.size() > 0 && ServerStatus.thisObj.server_info.get("crushSSO_tokens") != null) {
                            SharedSessionReplicated.send(Common.makeBoundary(), "SYNC_CRUSHSSO_TOKENS", "tokens", ServerStatus.thisObj.server_info.get("crushSSO_tokens"));
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                    }
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:SINGLETON")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    p2.put("singleton_id", DMZServerCommon.this.singleton_id);
                    DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:RESET_TOKEN_PASS")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Properties server_item2 = (Properties)DMZServerCommon.this.server_item.clone();
                    if (!p2.getProperty("internal_port", "").equals("")) {
                        Vector v = ServerStatus.VG("server_list");
                        int x = 0;
                        while (x < v.size()) {
                            Properties server_item3 = (Properties)v.elementAt(x);
                            if ((server_item3.getProperty("serverType").equalsIgnoreCase("HTTP") || server_item3.getProperty("serverType").equalsIgnoreCase("HTTPS")) && server_item3.getProperty("port").equals(p2.getProperty("internal_port"))) {
                                server_item2 = server_item3;
                                break;
                            }
                            ++x;
                        }
                    } else if (DMZServerCommon.this.server_item.getProperty("linkedServer", "").equals("@AutoHostHttp")) {
                        try {
                            VRL vrl = new VRL(p2.getProperty("currentURL"));
                            Vector v = ServerStatus.VG("login_page_list");
                            int x = 0;
                            while (x < v.size()) {
                                Properties p = (Properties)v.elementAt(x);
                                if (com.crushftp.client.Common.do_search(p.getProperty("domain"), vrl.getHost(), false, 0)) {
                                    String stem = p.getProperty("page").substring(0, p.getProperty("page").lastIndexOf("."));
                                    server_item2.put("linkedServer", stem);
                                }
                                ++x;
                            }
                        }
                        catch (Exception e) {
                            Log.log("HTTP_SERVER", 1, e);
                        }
                    }
                    String responseText = ServerSessionAJAX.doResetTokenPass(p2.getProperty("resetToken"), server_item2.getProperty("linkedServer", ""), p2.getProperty("password1"), new Properties());
                    p2.put("responseText", responseText);
                    DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:DOWNLOAD_COUNT")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    p2.put("responseText", String.valueOf(ServerStatus.thisObj.statTools.getUserDownloadCount(p2.getProperty("username"))));
                    DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:ACL")) {
            String item_privs = "";
            if (ServerStatus.SG("acl_mode").equals("2")) {
                item_privs = VFS.getAcl2Proc((Properties)p.get("dir_item"), p.getProperty("acl_domain"), p.getProperty("localPath"), p.getProperty("username"));
            } else if (ServerStatus.SG("acl_mode").equals("3")) {
                item_privs = VFS.getAcl3Proc((Properties)p.get("dir_item"), p.getProperty("acl_domain"), p.getProperty("localPath"), p.getProperty("username"), new Properties());
            }
            p.put("item_privs", item_privs);
            DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:SHARE")) {
            Properties request = (Properties)p.remove("request");
            SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                String response = "";
                try {
                    Vector<Properties> path_items = new Vector<Properties>();
                    String[] paths = Common.url_decode(request.getProperty("paths")).split("\r\n");
                    int x = 0;
                    while (x < paths.length) {
                        String the_dir = paths[x].trim();
                        if (!the_dir.equals("")) {
                            if (the_dir.startsWith(thisSession.SG("root_dir"))) {
                                the_dir = the_dir.substring(thisSession.SG("root_dir").length() - 1);
                            }
                            String path = thisSession.getStandardizedDir(the_dir);
                            Log.log("HTTP_SERVER", 2, "Sharing:" + the_dir + "  vs.  " + path);
                            Properties item = thisSession.uVFS.get_item(path);
                            Log.log("HTTP_SERVER", 2, "Sharing:" + item);
                            VRL vrl = new VRL(item.getProperty("url"));
                            Properties stat = null;
                            GenericClient c = thisSession.uVFS.getClient(item);
                            try {
                                stat = c.stat(vrl.getPath());
                                stat.put("root_dir", item.getProperty("root_dir"));
                            }
                            finally {
                                c = thisSession.uVFS.releaseClient(c);
                            }
                            stat.put("privs", item.getProperty("privs"));
                            path_items.addElement(stat);
                        }
                        ++x;
                    }
                    response = ServerSessionAJAX.createShare(path_items, request, (Vector)thisSession.user.get("web_customizations"), thisSession.uiSG("user_name"), thisSession.server_item.getProperty("linkedServer"), thisSession.user, thisSession.date_time, thisSession);
                }
                catch (Exception e) {
                    Log.log("DMZ", 0, e);
                }
                p.put("object_response", response);
                DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
            }
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:GETMD5S")) {
            final Properties p2 = p;
            final Properties request2 = (Properties)p.remove("request");
            final SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                try {
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            String path_str = null;
                            try {
                                path_str = new String(Base64.decode(request2.getProperty("path")));
                            }
                            catch (Exception e) {
                                path_str = com.crushftp.client.Common.dots(Common.url_decode(request2.getProperty("path")));
                            }
                            if (!path_str.equals("")) {
                                StringBuffer responseBuf;
                                Vector md5s;
                                block10: {
                                    if (path_str.startsWith(thisSession.SG("root_dir"))) {
                                        path_str = path_str.substring(thisSession.SG("root_dir").length() - 1);
                                    }
                                    md5s = new Vector();
                                    responseBuf = new StringBuffer();
                                    try {
                                        Properties item = thisSession.uVFS.get_item(String.valueOf(thisSession.SG("root_dir")) + path_str);
                                        if (item == null) break block10;
                                        GenericClient c = thisSession.uVFS.getClient(item);
                                        try {
                                            VRL vrl = new VRL(item.getProperty("url"));
                                            Properties stat = c.stat(vrl.getPath());
                                            Common.getMD5(c.download(vrl.getPath(), 0L, -1L, true), md5s, request2.getProperty("chunked", "true").equals("true"), request2.getProperty("forward", "true").equals("true"), Long.parseLong(stat.getProperty("size")), Long.parseLong(request2.getProperty("local_size", "0")));
                                        }
                                        finally {
                                            c = thisSession.uVFS.releaseClient(c);
                                        }
                                    }
                                    catch (Exception e) {
                                        Log.log("HTTP_SERVER", 1, e);
                                    }
                                }
                                while (md5s.size() > 0) {
                                    responseBuf.append(md5s.remove(0).toString()).append("\r\n");
                                }
                                p2.put("object_response", responseBuf.toString());
                                DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                            }
                        }
                    });
                }
                catch (Exception e) {
                    Log.log("DMZ", 0, e);
                }
            }
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:CUSTOM")) {
            Properties request = (Properties)p.remove("request");
            SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                String response = "";
                try {
                    Vector<Properties> path_items = new Vector<Properties>();
                    String[] paths = null;
                    paths = request.getProperty("paths").indexOf("|") >= 0 ? Common.url_decode(request.getProperty("paths")).split("\\|") : (request.getProperty("paths").indexOf(";") >= 0 ? Common.url_decode(request.getProperty("paths")).split(";") : Common.url_decode(request.getProperty("paths")).split("\r\n"));
                    int x = 0;
                    while (x < paths.length) {
                        String the_dir = paths[x].trim();
                        if (!the_dir.equals("")) {
                            if (the_dir.startsWith(thisSession.SG("root_dir"))) {
                                the_dir = the_dir.substring(thisSession.SG("root_dir").length() - 1);
                            }
                            String path = thisSession.getStandardizedDir(the_dir);
                            Log.log("HTTP_SERVER", 2, "Custom:" + the_dir + "  vs.  " + path);
                            Properties item = thisSession.uVFS.get_item(path);
                            Log.log("HTTP_SERVER", 2, "Custom:" + item);
                            VRL vrl = new VRL(item.getProperty("url"));
                            Properties stat = null;
                            GenericClient c = thisSession.uVFS.getClient(item);
                            try {
                                stat = c.stat(vrl.getPath());
                            }
                            finally {
                                c = thisSession.uVFS.releaseClient(c);
                            }
                            stat.put("privs", item.getProperty("privs"));
                            String root_dir = Common.all_but_last(the_dir);
                            stat.put("root_dir", root_dir);
                            path_items.addElement(stat);
                        }
                        ++x;
                    }
                    String common_root = "";
                    int depth = 0;
                    while (true) {
                        boolean all_ok = true;
                        String root_dir = ((Properties)path_items.elementAt(0)).getProperty("root_dir");
                        String new_common_root = "/";
                        if (depth >= root_dir.split("/").length) break;
                        int x2 = 0;
                        while (x2 < depth) {
                            new_common_root = String.valueOf(new_common_root) + root_dir.split("/")[x2 + 1] + "/";
                            ++x2;
                        }
                        ++depth;
                        x2 = 0;
                        while (x2 < path_items.size()) {
                            Properties pp = (Properties)path_items.elementAt(x2);
                            if (!pp.getProperty("root_dir").startsWith(new_common_root)) {
                                all_ok = false;
                            }
                            ++x2;
                        }
                        if (!all_ok || common_root.equals(new_common_root)) break;
                        common_root = new_common_root;
                    }
                    if (common_root.equals("")) {
                        common_root = "/";
                    }
                    common_root = common_root.substring(0, common_root.length() - 1);
                    int x3 = 0;
                    while (x3 < path_items.size()) {
                        Properties pp = (Properties)path_items.elementAt(x3);
                        String root_dir = pp.getProperty("root_dir");
                        if (root_dir.startsWith(common_root)) {
                            root_dir = root_dir.substring(common_root.length());
                        }
                        pp.put("root_dir", root_dir);
                        ++x3;
                    }
                    response = ServerSessionAJAX.createCustom(path_items, request, thisSession);
                }
                catch (Exception e) {
                    Log.log("DMZ", 0, e);
                }
                p.put("object_response", response);
                DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
            }
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:PROBLEM")) {
            Properties request = (Properties)p.remove("request");
            SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                String response = "";
                try {
                    Vector<Properties> path_items = new Vector<Properties>();
                    String[] paths = null;
                    paths = request.getProperty("paths").indexOf("|") >= 0 ? Common.url_decode(request.getProperty("paths")).split("\\|") : (request.getProperty("paths").indexOf(";") >= 0 ? Common.url_decode(request.getProperty("paths")).split(";") : Common.url_decode(request.getProperty("paths")).split("\r\n"));
                    int x = 0;
                    while (x < paths.length) {
                        String the_dir = paths[x].trim();
                        if (!the_dir.equals("")) {
                            if (the_dir.startsWith(thisSession.SG("root_dir"))) {
                                the_dir = the_dir.substring(thisSession.SG("root_dir").length() - 1);
                            }
                            String path = thisSession.getStandardizedDir(the_dir);
                            Log.log("HTTP_SERVER", 2, "Problem:" + the_dir + "  vs.  " + path);
                            Properties item = thisSession.uVFS.get_item(path);
                            Log.log("HTTP_SERVER", 2, "Problem:" + item);
                            VRL vrl = new VRL(item.getProperty("url"));
                            Properties stat = null;
                            GenericClient c = thisSession.uVFS.getClient(item);
                            try {
                                stat = c.stat(vrl.getPath());
                            }
                            finally {
                                c = thisSession.uVFS.releaseClient(c);
                            }
                            stat.put("privs", item.getProperty("privs"));
                            String root_dir = Common.all_but_last(the_dir);
                            stat.put("root_dir", root_dir);
                            path_items.addElement(stat);
                        }
                        ++x;
                    }
                    String common_root = "";
                    int depth = 0;
                    while (true) {
                        boolean all_ok = true;
                        String root_dir = ((Properties)path_items.elementAt(0)).getProperty("root_dir");
                        String new_common_root = "/";
                        if (depth >= root_dir.split("/").length) break;
                        int x4 = 0;
                        while (x4 < depth) {
                            new_common_root = String.valueOf(new_common_root) + root_dir.split("/")[x4 + 1] + "/";
                            ++x4;
                        }
                        ++depth;
                        x4 = 0;
                        while (x4 < path_items.size()) {
                            Properties pp = (Properties)path_items.elementAt(x4);
                            if (!pp.getProperty("root_dir").startsWith(new_common_root)) {
                                all_ok = false;
                            }
                            ++x4;
                        }
                        if (!all_ok || common_root.equals(new_common_root)) break;
                        common_root = new_common_root;
                    }
                    if (common_root.equals("")) {
                        common_root = "/";
                    }
                    common_root = common_root.substring(0, common_root.length() - 1);
                    int x5 = 0;
                    while (x5 < path_items.size()) {
                        Properties pp = (Properties)path_items.elementAt(x5);
                        String root_dir = pp.getProperty("root_dir");
                        if (root_dir.startsWith(common_root)) {
                            root_dir = root_dir.substring(common_root.length());
                        }
                        pp.put("root_dir", root_dir);
                        ++x5;
                    }
                    response = ServerSessionAJAX.createCustom(path_items, request, thisSession);
                }
                catch (Exception e) {
                    Log.log("DMZ", 0, e);
                }
                p.put("object_response", response);
                DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
            }
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:BATCH_COMPLETE")) {
            SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                thisSession.do_event5("BATCH_COMPLETE", null);
            }
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:MANAGESHARES")) {
            p.remove("request");
            p.put("object_response", "");
            SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                String response = ServerSessionAJAX.manageShares(thisSession);
                p.put("object_response", response);
            }
            DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:GETHISTORY")) {
            Properties request = (Properties)p.remove("request");
            p.put("object_response", "");
            SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                String response = ServerSessionAJAX.getHistory(request, thisSession);
                p.put("object_response", response);
            }
            DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:DELETESHARE")) {
            Properties request = (Properties)p.remove("request");
            p.put("object_response", "");
            SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                String response = ServerSessionAJAX.deleteShare(request, thisSession);
                p.put("object_response", response);
            }
            DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:EDITSHARE")) {
            Properties request = (Properties)p.remove("request");
            p.put("object_response", "");
            SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                String response = ServerSessionAJAX.editShare(request, thisSession);
                p.put("object_response", response);
            }
            DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:MESSAGEFORM")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Properties request = (Properties)p2.remove("request");
                    SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p2.getProperty("crushAuth"));
                    if (thisSession != null && thisSession.uVFS != null) {
                        String response = ServerSessionAJAX.handle_message_form(request, thisSession);
                        p2.put("object_response", response);
                        DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                    }
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:SELFREGISTRATION")) {
            Properties request = (Properties)p.remove("request");
            String req_id = p.remove("req_id").toString();
            p.put("object_response", "");
            SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                String response = ServerSessionAJAX.selfRegistration(request, thisSession, req_id);
                p.put("object_response", response);
            }
            DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:ERROR_EVENT")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Properties error_info = (Properties)p2.get("error_info");
                    SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p2.getProperty("crushAuth"));
                    if (thisSession != null && thisSession.uVFS != null) {
                        Properties ui2 = (Properties)p2.get("error_user_info");
                        Enumeration<Object> keys = ui2.keys();
                        while (keys.hasMoreElements()) {
                            String key = "" + keys.nextElement();
                            boolean allowed = false;
                            if (key.startsWith("as2") || key.equals("host") || key.equals("message-id") || key.equals("content_length") || key.startsWith("disp") || key.equals("accept") || key.equals("signMdn")) {
                                allowed = true;
                            }
                            if (thisSession.user_info.containsKey(key) && !allowed) continue;
                            thisSession.user_info.put(key, ui2.get(key));
                        }
                        thisSession.do_event5("ERROR", error_info);
                    }
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:AS2MDN")) {
            final Properties mdnInfo = (Properties)p.get("mdnInfo");
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    As2Msg.mdnResponses.put(mdnInfo.getProperty("Original-Message-ID".toLowerCase()), mdnInfo);
                    try {
                        Thread.sleep(5000L);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    As2Msg.mdnResponses.remove(mdnInfo.getProperty("Original-Message-ID".toLowerCase()));
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:PONG")) {
            Properties pong = (Properties)p.remove("data");
            Log.log("DMZ", 1, "DMZ command queue ping:" + (System.currentTimeMillis() - Long.parseLong(pong.getProperty("time"))) + "ms");
            this.last_pong = System.currentTimeMillis();
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:ALERT")) {
            final Properties alert_info = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    ServerStatus.thisObj.runAlerts(alert_info.getProperty("alert_action"), (Properties)alert_info.get("info"), (Properties)alert_info.get("user_info"), (Properties)alert_info.get("user"), null, (Properties)alert_info.get("alert"), true);
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:SYNC")) {
            Log.log("DMZ", 2, "READ:" + this.server_item.getProperty("server_item_name") + ":" + p.getProperty("type") + ":" + p);
            Properties user = UserTools.ut.getUser(this.server_item.getProperty("linkedServer", ""), p.getProperty("username"), true);
            VFS uVFS = UserTools.ut.getVFS(this.server_item.getProperty("linkedServer", ""), p.getProperty("username"));
            if (user != null) {
                Properties request = (Properties)p.remove("request");
                if (request.getProperty("command", "").equalsIgnoreCase("getSyncTableData")) {
                    final Properties request2 = request;
                    final Properties p2 = p;
                    final VFS uVFS2 = uVFS;
                    final Properties user2 = user;
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            Thread.currentThread().setName("DMZ:getSyncTableData:" + request2.toString());
                            String vfs_path = request2.getProperty("path", "");
                            String root_dir = SessionCrush.getRootDir(null, uVFS2, user2, false);
                            if (vfs_path.equals("")) {
                                vfs_path = "/";
                            }
                            if (!vfs_path.startsWith(root_dir)) {
                                vfs_path = String.valueOf(root_dir) + vfs_path.substring(1);
                            }
                            try {
                                Vector o = Common.getSyncTableData(p2.getProperty("syncID").toUpperCase(), Long.parseLong(request2.getProperty("lastRID")), request2.getProperty("table"), p2.getProperty("clientid"), vfs_path, uVFS2);
                                if (o != null) {
                                    p2.put("object_response", o);
                                }
                                Log.log("DMZ", 2, "READ:" + DMZServerCommon.this.server_item.getProperty("server_item_name") + ":" + p2.getProperty("type") + ":GOT RESPONSE, sending back.");
                                DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                            }
                            catch (Exception e) {
                                Log.log("DMZ", 0, e);
                            }
                        }
                    });
                } else if (request.getProperty("command", "").equalsIgnoreCase("syncConflict")) {
                    SyncTools.addJournalEntry(p.getProperty("syncID"), request.getProperty("item_path"), "CONFLICT", "", "");
                    DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
                } else if (request.getProperty("command", "").equalsIgnoreCase("purgeSync")) {
                    String root_dir = p.getProperty("root_dir");
                    if (root_dir.indexOf("/", 1) > 0) {
                        root_dir = p.getProperty("root_dir").substring(p.getProperty("root_dir").indexOf("/", 1));
                    }
                    AdminControls.purgeSync(request, uVFS, root_dir);
                    DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
                }
            } else {
                DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
            }
        } else if (p.getProperty("type").equalsIgnoreCase("GET:QUOTA")) {
            Log.log("DMZ", 2, "READ:" + this.server_item.getProperty("server_item_name") + ":" + p.getProperty("type") + ":" + p);
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    String q = "-12345";
                    try {
                        SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p2.getProperty("crushAuth"));
                        if (thisSession != null && thisSession.uVFS != null) {
                            String root_dir = SessionCrush.getRootDir(null, thisSession.uVFS, null, true);
                            String the_dir = String.valueOf(root_dir) + p2.getProperty("the_dir").substring(1);
                            q = String.valueOf(SessionCrush.get_quota(the_dir, thisSession.uVFS, "", new Properties(), null, true));
                            q = String.valueOf(q) + ":" + SessionCrush.get_quota(the_dir, thisSession.uVFS, "", new Properties(), null, false);
                        }
                    }
                    catch (Exception e) {
                        Log.log("DMZ", 1, e);
                    }
                    p2.put("object_response", String.valueOf(q));
                    DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:QUOTA_USED")) {
            Log.log("DMZ", 2, "READ:" + this.server_item.getProperty("server_item_name") + ":" + p.getProperty("type") + ":" + p);
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    long q = -12345L;
                    try {
                        SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p2.getProperty("crushAuth"));
                        if (thisSession != null && thisSession.uVFS != null) {
                            String root_dir = SessionCrush.getRootDir(null, thisSession.uVFS, null, true);
                            String the_dir = String.valueOf(root_dir) + p2.getProperty("the_dir").substring(1);
                            q = SessionCrush.get_quota_used(the_dir, thisSession.uVFS, "", null);
                        }
                    }
                    catch (Exception e) {
                        Log.log("DMZ", 1, e);
                    }
                    p2.put("object_response", String.valueOf(q));
                    DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:CHANGE_PASSWORD")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    p2.put("object_response", "");
                    SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p2.getProperty("crushAuth", ""));
                    if (thisSession == null) {
                        try {
                            Properties server_item_temp = null;
                            int x = 0;
                            while (x < ServerStatus.VG("server_list").size()) {
                                Properties si = (Properties)ServerStatus.VG("server_list").elementAt(x);
                                if (si.getProperty("serverType").startsWith("HTTP") && si.getProperty("port").equals(String.valueOf(Integer.parseInt(p2.getProperty("preferred_port", "0"))))) {
                                    server_item_temp = si;
                                    break;
                                }
                                ++x;
                            }
                            if (server_item_temp == null) {
                                Log.log("SERVER", 2, "GET:SOCKET:Prefered port not found...finding first HTTP(s) item to use...");
                                x = 0;
                                while (x < ServerStatus.VG("server_list").size()) {
                                    server_item_temp = (Properties)ServerStatus.VG("server_list").elementAt(x);
                                    if (server_item_temp.getProperty("serverType").equals("HTTP") || server_item_temp.getProperty("serverType").equals("HTTPS")) break;
                                    ++x;
                                }
                            }
                            if (server_item_temp == null) {
                                server_item_temp = DMZServerCommon.this.server_item;
                            }
                            SessionCrush tempSession = new SessionCrush(null, 1, "127.0.0.1", 0, "0.0.0.0", server_item_temp.getProperty("linkedServer", ""), server_item_temp);
                            try {
                                tempSession.verify_user(p2.getProperty("username"), p2.getProperty("current_password"), false, false);
                                tempSession.put("user_name", p2.getProperty("username"));
                                thisSession = tempSession;
                            }
                            catch (Exception e) {
                                Log.log("DMZ", 1, e);
                            }
                        }
                        catch (Exception e) {
                            Log.log("DMZ", 0, e);
                        }
                    }
                    if (thisSession != null && thisSession.uVFS != null) {
                        Properties request = (Properties)p2.remove("request");
                        String response = ServerSessionAJAX.changePassword(request, (String)p2.remove("site"), thisSession);
                        p2.put("object_response", response);
                    }
                    DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:EDIT_KEYWORDS")) {
            Properties request = (Properties)p.remove("request");
            p.put("object_response", "");
            SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("crushAuth"));
            if (thisSession != null && thisSession.uVFS != null) {
                String response = ServerSessionAJAX.processKeywordsEdit(request, thisSession);
                p.put("object_response", response);
            }
            DMZServerCommon.sendCommand(this.server_item.getProperty("server_item_name"), p, "RESPONSE", p.getProperty("id"));
        } else if (p.getProperty("type").equalsIgnoreCase("GET:SEARCH")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Properties request = (Properties)p2.remove("request");
                    p2.put("object_response", "");
                    SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p2.getProperty("crushAuth"));
                    if (thisSession != null && thisSession.uVFS != null) {
                        String response = "";
                        try {
                            response = ServerSessionAJAX.search(request, thisSession);
                        }
                        catch (Exception e) {
                            response = "" + e;
                            Log.log("DMZ", 1, e);
                        }
                        p2.put("object_response", response);
                        DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                    }
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:SEARCH_STATUS")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    p2.put("object_response", "");
                    SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p2.getProperty("crushAuth"));
                    if (thisSession != null && thisSession.uVFS != null) {
                        p2.put("object_response", thisSession.uiSG("search_status").trim());
                        thisSession.uVFS.reset();
                        DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                    }
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:HANDLE_CUSTOMIZATIONS")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Vector customizations = (Vector)p2.remove("customizations");
                    p2.put("object_response", "");
                    SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p2.getProperty("crushAuth"));
                    if (thisSession != null && thisSession.uVFS != null) {
                        String response = "";
                        try {
                            int x = 0;
                            while (x < customizations.size()) {
                                Properties pp = (Properties)customizations.elementAt(x);
                                if (pp.getProperty("value", "").contains("{user_listen_ip}")) {
                                    String value = Common.replace_str(pp.getProperty("value", ""), "{user_listen_ip}", "{user_dmz_listen_ip}");
                                    pp.put("value", value);
                                }
                                ++x;
                            }
                            ServerSessionAJAX.handleCustomizations(customizations, thisSession);
                        }
                        catch (Exception e) {
                            response = "" + e;
                            Log.log("DMZ", 1, e);
                        }
                        p2.put("object_response", customizations);
                    }
                    DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("PUT:TWO_FACTOR_SECRET")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    p2.put("response", "");
                    String response = "Success";
                    try {
                        UserTools.ut.put_in_user(p2.getProperty("linkedServer"), p2.getProperty("username"), "twofactor_secret", ServerStatus.thisObj.common_code.encode_pass(p2.getProperty("generatedKey"), "DES", ""), true, true);
                    }
                    catch (Exception e) {
                        response = "" + e;
                        Log.log("DMZ", 1, e);
                    }
                    p2.put("response", response);
                    DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                }
            });
        } else if (p.getProperty("type").equalsIgnoreCase("GET:ENCRYPTED_PASS")) {
            final Properties p2 = p;
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    p2.put("response", "");
                    String encryptedPass = "";
                    try {
                        String encrypt_type = p2.getProperty("encrypt_type", "");
                        if (encrypt_type.trim().equals("")) {
                            encrypt_type = ServerStatus.SG("password_encryption");
                        }
                        encryptedPass = ServerStatus.thisObj.common_code.encode_pass(p2.getProperty("password", ""), encrypt_type, "");
                    }
                    catch (Exception e) {
                        Log.log("DMZ", 1, e);
                    }
                    p2.put("response", encryptedPass);
                    DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), p2, "RESPONSE", p2.getProperty("id"));
                }
            });
        }
    }

    public Runnable getSocketConnector() {
        Runnable r1 = new Runnable(){

            @Override
            public void run() {
                Thread.currentThread().setPriority(1);
                StringBuffer die_now2 = DMZServerCommon.this.die_now;
                try {
                    try {
                        Vector pending_data_socks = new Vector();
                        SSLSocketFactory factory = ServerStatus.thisObj.common_code.getSSLContext("builtin", null, "crushftp", "crushftp", "TLS", false, true).getSocketFactory();
                        int logged_idle = 0;
                        while (die_now2.length() == 0) {
                            if (ServerStatus.BG("serverbeat_dmz_master") && !ServerBeat.current_master) {
                                Thread.sleep(1000L);
                                continue;
                            }
                            Socket tempSock = null;
                            while (pending_data_socks.size() >= ServerStatus.IG("dmz_socket_pool_size")) {
                                logged_idle = 0;
                                Thread.sleep(100L);
                            }
                            if (pending_data_socks.size() < 10) {
                                if (++logged_idle < 5) {
                                    Log.log("SERVER", 2, "Having trouble keeping up to DMZ socket demand, idle sockets=" + pending_data_socks.size() + " max size=" + ServerStatus.IG("dmz_socket_pool_size"));
                                }
                                if (logged_idle > 1000) {
                                    logged_idle = 0;
                                }
                            }
                            try {
                                boolean tunneled;
                                boolean bl = tunneled = DMZServerCommon.this.server_item.getProperty("tunneled", "false").equals("true") || DMZServerCommon.this.server_item.getProperty("dmz_version", "").equals("4") || DMZServerCommon.this.server_item.getProperty("dmz_version", "").equals("5");
                                if (System.getProperty("crushftp.dmz.ssl", "true").equals("true") && !tunneled) {
                                    tempSock = (SSLSocket)factory.createSocket(DMZServerCommon.this.server_item.getProperty("ip"), Integer.parseInt(DMZServerCommon.this.server_item.getProperty("port")));
                                    Common.configureSSLTLSSocket((SSLSocket)tempSock);
                                } else {
                                    tempSock = new Socket();
                                    tempSock.setSoTimeout(1000);
                                    int loops = 0;
                                    while (loops++ < 30) {
                                        try {
                                            if (tunneled) {
                                                tempSock.connect(new InetSocketAddress("127.0.0.1", DMZServerCommon.this.dmz_tunnel_client_d.getLocalPort()));
                                                break;
                                            }
                                            tempSock.connect(new InetSocketAddress(DMZServerCommon.this.server_item.getProperty("ip"), Integer.parseInt(DMZServerCommon.this.server_item.getProperty("port"))));
                                            break;
                                        }
                                        catch (SocketTimeoutException e) {
                                            if (loops >= 29) {
                                                throw e;
                                            }
                                            Log.log("DMZ", 1, "Timeout #" + loops + " of 1 second when trying to establish conenction to DMZ..." + e);
                                        }
                                    }
                                }
                                tempSock.setTcpNoDelay(true);
                                com.crushftp.client.Common.sockLog(tempSock, "tempSock create.  pending_data_socks size=" + pending_data_socks.size());
                                if (DMZServerCommon.this.checkLoggingSockneeded(tempSock)) continue;
                                DMZServerCommon.this.processDataSocket(tempSock, pending_data_socks);
                            }
                            catch (IOException e) {
                                com.crushftp.client.Common.sockLog(tempSock, "tempSock IOException:" + e);
                                Log.log("DMZ", 2, e);
                                Thread.sleep(200L);
                                com.crushftp.client.Common.sockLog(tempSock, "tempSock closing.  pending_data_socks size=" + pending_data_socks.size());
                                if (tempSock == null) continue;
                                tempSock.close();
                            }
                        }
                    }
                    catch (Exception e) {
                        Log.log("DMZ", 0, e);
                        DMZServerCommon.this.socket_created = false;
                        die_now2.append(System.currentTimeMillis());
                        Thread.currentThread().setPriority(5);
                    }
                }
                finally {
                    Thread.currentThread().setPriority(5);
                }
            }
        };
        return r1;
    }

    public Runnable getSocketReceiver() {
        Runnable r2 = new Runnable(){

            @Override
            public void run() {
                StringBuffer die_now2 = DMZServerCommon.this.die_now;
                Properties in_out = (Properties)DMZServerCommon.this.socks_in_out.get(DMZServerCommon.this.read_sock);
                try {
                    try {
                        DataInputStream din = (DataInputStream)in_out.get("in");
                        DataOutputStream dout = (DataOutputStream)in_out.get("out");
                        while (die_now2.length() == 0) {
                            try {
                                long start = System.currentTimeMillis();
                                int len = din.readInt();
                                byte[] b = new byte[len];
                                int bytesRead = 0;
                                int totalBytes = 0;
                                while (totalBytes < len) {
                                    bytesRead = din.read(b, totalBytes, len - totalBytes);
                                    if (bytesRead < 0) {
                                        throw new Exception("DMZ:EOF reached in receiver read of chunk.");
                                    }
                                    totalBytes += bytesRead;
                                }
                                dout.writeInt(totalBytes);
                                long end = System.currentTimeMillis();
                                if (len > 0) {
                                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
                                    Properties p = (Properties)ois.readObject();
                                    ois.close();
                                    Thread.currentThread().setName("DMZSender:responseQueue=" + DMZServerCommon.this.responseQueue.size() + " last write len=" + len + "(" + p.getProperty("type") + ") milliseconds=" + (end - start) + ", total millis=" + (System.currentTimeMillis() - start) + " last_write_info:" + DMZServerCommon.this.last_write_info);
                                    DMZServerCommon.this.responseQueue.addElement(p);
                                    ++DMZServerCommon.this.messages_received;
                                }
                            }
                            catch (SocketTimeoutException start) {
                                // empty catch block
                            }
                            if (System.currentTimeMillis() - DMZServerCommon.this.last_ping > 10000L) {
                                Properties ping = new Properties();
                                ping.put("id", Common.makeBoundary());
                                ping.put("time", String.valueOf(System.currentTimeMillis()));
                                DMZServerCommon.this.last_ping = System.currentTimeMillis();
                                DMZServerCommon.sendCommand(DMZServerCommon.this.server_item.getProperty("server_item_name"), ping, "PUT:PING", ping.getProperty("id"));
                            }
                            if (System.currentTimeMillis() - DMZServerCommon.this.last_ping <= ServerStatus.LG("dmz_pong_timeout") * 1000L) continue;
                            Log.log("DMZ", 0, "Socket timeout " + ServerStatus.IG("dmz_pong_timeout") + " seconds, firewall killed socket.  last_ping=" + new Date(DMZServerCommon.this.last_ping));
                            if (Log.log("DMZ", 1, "")) {
                                com.crushftp.client.Common.sockLog(DMZServerCommon.this.read_sock, com.crushftp.client.Common.dumpStack("1:Socket timeout, firewall killed socket.  last_ping=" + new Date(DMZServerCommon.this.last_ping)));
                            }
                            throw new Exception("Socket timeout, firewall killed socket.");
                        }
                    }
                    catch (Throwable e) {
                        com.crushftp.client.Common.sockLog(DMZServerCommon.this.read_sock, "Failure with read_socket:" + e);
                        Log.log("DMZ", 0, e);
                        try {
                            DMZServerCommon.closeInOutSockRef(DMZServerCommon.this.socks_in_out, DMZServerCommon.this.sock);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        DMZServerCommon.this.socket_created = false;
                        DMZServerCommon.this.restart = true;
                        die_now2.append(System.currentTimeMillis());
                        Thread.currentThread().setPriority(5);
                    }
                }
                finally {
                    Thread.currentThread().setPriority(5);
                }
            }
        };
        return r2;
    }

    public Runnable getResponseProcessor() {
        Runnable r3 = new Runnable(){

            /*
             * Unable to fully structure code
             */
            @Override
            public void run() {
                Thread.currentThread().setPriority(10);
                die_now2 = DMZServerCommon.this.die_now;
                try {
                    try {
                        last_response_clean = System.currentTimeMillis();
                        ** GOTO lbl24
                        {
                            DMZServerCommon.this.processResponse((Properties)DMZServerCommon.this.responseQueue.remove(0));
                            do {
                                if (DMZServerCommon.this.responseQueue.size() > 0) continue block5;
                                if (System.currentTimeMillis() - last_response_clean > 60000L) {
                                    keys = DMZServerCommon.dmzResponses.keys();
                                    while (keys.hasMoreElements()) {
                                        id = keys.nextElement().toString();
                                        p = (Properties)DMZServerCommon.dmzResponses.get(id);
                                        if (p == null) continue;
                                        received = Long.parseLong(p.getProperty("received", "0"));
                                        if (System.currentTimeMillis() - received <= 60000L) continue;
                                        DMZServerCommon.dmzResponses.remove(id);
                                        p.clear();
                                    }
                                    last_response_clean = System.currentTimeMillis();
                                }
                                Thread.sleep(10L);
lbl24:
                                // 2 sources

                            } while (die_now2.length() == 0);
                        }
                    }
                    catch (Exception e) {
                        Log.log("DMZ", 0, e);
                        DMZServerCommon.this.socket_created = false;
                        die_now2.append(System.currentTimeMillis());
                        Thread.currentThread().setPriority(5);
                    }
                }
                finally {
                    Thread.currentThread().setPriority(5);
                }
            }
        };
        return r3;
    }

    public Runnable getLoggingSocket() {
        Runnable r4 = new Runnable(){

            /*
             * Enabled aggressive block sorting
             * Enabled unnecessary exception pruning
             * Enabled aggressive exception aggregation
             */
            @Override
            public void run() {
                Thread.currentThread().setPriority(1);
                StringBuffer die_now2 = DMZServerCommon.this.die_now;
                try {
                    try {
                        while (true) {
                            if (die_now2.length() != 0) {
                                return;
                            }
                            try {
                                if (DMZServerCommon.this.logging_socket_ois != null) {
                                    Properties p = (Properties)DMZServerCommon.this.logging_socket_ois.readObject();
                                    Log.log("DMZ", Integer.parseInt(p.getProperty("level")), String.valueOf(DMZServerCommon.this.server_item.getProperty("server_item_name")) + ": " + p.getProperty("tag") + ": " + p.getProperty("message"), Long.parseLong(p.getProperty("t")));
                                    continue;
                                }
                                Thread.sleep(1000L);
                            }
                            catch (SocketTimeoutException p) {
                            }
                            catch (Exception e) {
                                try {
                                    if (DMZServerCommon.this.logging_socket_ois != null) {
                                        DMZServerCommon.this.logging_socket_ois.close();
                                    }
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                try {
                                    if (DMZServerCommon.this.logging_socket != null) {
                                        DMZServerCommon.this.logging_socket.close();
                                    }
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                DMZServerCommon.this.logging_socket_ois = null;
                                DMZServerCommon.this.logging_socket = null;
                                Log.log("DMZ", 0, e);
                            }
                        }
                    }
                    catch (Exception e) {
                        Log.log("DMZ", 0, e);
                        DMZServerCommon.this.socket_created = false;
                        die_now2.append(System.currentTimeMillis());
                        Thread.currentThread().setPriority(5);
                        return;
                    }
                }
                finally {
                    Thread.currentThread().setPriority(5);
                }
            }
        };
        return r4;
    }

    public boolean checkLoggingSockneeded(final Socket tempSock2) {
        if (this.logging_socket == null && this.logging_socket_ois == null && System.currentTimeMillis() - this.last_logging_socket_time > 10000L) {
            if (ServerStatus.BG("dmz_log_in_internal_server")) {
                this.last_logging_socket_time = System.currentTimeMillis();
                this.logging_socket = tempSock2;
                com.crushftp.client.Common.sockLog(this.logging_socket, "tempSock becoming logging socket.");
                try {
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                DMZServerCommon.this.logging_socket.setSoTimeout(1000);
                                DMZServerCommon.this.logging_socket.getOutputStream().write("L".getBytes());
                                DMZServerCommon.this.logging_socket.getOutputStream().flush();
                                DMZServerCommon.this.logging_socket_ois = new ObjectInputStream(DMZServerCommon.this.logging_socket.getInputStream());
                            }
                            catch (Exception e) {
                                com.crushftp.client.Common.sockLog(tempSock2, "tempSock2 IOException:" + e);
                                Log.log("DMZ", 2, e);
                                try {
                                    if (tempSock2 != null) {
                                        com.crushftp.client.Common.sockLog(DMZServerCommon.this.logging_socket, "logging socket closing.");
                                        tempSock2.close();
                                    }
                                }
                                catch (IOException iOException) {
                                    // empty catch block
                                }
                            }
                        }
                    });
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                return true;
            }
        }
        return false;
    }

    public void processDataSocket(final Socket tempSock2, final Vector pending_data_socks) throws SocketException {
        boolean tunneled;
        pending_data_socks.addElement(tempSock2);
        boolean bl = tunneled = this.server_item.getProperty("tunneled", "false").equals("true") || this.server_item.getProperty("dmz_version", "").equals("4") || this.server_item.getProperty("dmz_version", "").equals("5");
        if (!tunneled) {
            tempSock2.setSoTimeout(MAX_DMZ_SOCKET_IDLE_TIME + 2000);
        }
        try {
            Worker.startWorker(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    try {
                        tempSock2.getOutputStream().write("d".getBytes());
                        tempSock2.getOutputStream().flush();
                        byte[] pb = new byte[100];
                        int pos = 0;
                        int bytesRead = 0;
                        while (pos < 100 && bytesRead >= 0) {
                            bytesRead = tempSock2.getInputStream().read(pb, pos, 100 - pos);
                            if (bytesRead <= 0) continue;
                            pos += bytesRead;
                        }
                        if (new String(pb).trim().equals("") || pos < 100) {
                            throw new IOException("DMZ socket received invalid prefered port. " + tempSock2);
                        }
                        com.crushftp.client.Common.sockLog(tempSock2, "tempSock2 create -> " + new String(pb) + ".   pending_data_socks size=" + pending_data_socks.size());
                        Thread.currentThread().setName("DMZ pending data sock:" + new String(pb).trim());
                        pending_data_socks.remove(tempSock2);
                        boolean reverse = false;
                        if (new String(pb).trim().indexOf(":") > 0) {
                            reverse = true;
                            if (!new String(pb).trim().split(":")[0].trim().equals("")) {
                                tempSock2.setSoTimeout(0);
                                Socket sock = new Socket(new String(pb).trim().split(":")[0], Integer.parseInt(new String(pb).trim().split(":")[1]));
                                sock.setTcpNoDelay(true);
                                if (new String(pb).trim().split(":").length > 2) {
                                    String inet_protocol = new String(pb).trim().split(":")[2].split("\\.").length == 4 ? "TCP4" : "TCP6";
                                    String proxy_protocol_v1 = "PROXY " + inet_protocol + " " + new String(pb).trim().split(":")[2] + " " + new String(pb).trim().split(":")[0] + " " + new String(pb).trim().split(":")[3] + " " + new String(pb).trim().split(":")[1] + "\r\n";
                                    sock.getOutputStream().write(proxy_protocol_v1.getBytes("UTF8"));
                                }
                                com.crushftp.client.Common.sockLog(sock, "sock create reverse");
                                com.crushftp.client.Common.streamCopier(sock, tempSock2, tempSock2.getInputStream(), sock.getOutputStream(), true, true, true);
                                com.crushftp.client.Common.streamCopier(sock, tempSock2, sock.getInputStream(), tempSock2.getOutputStream(), true, true, true);
                            } else {
                                com.crushftp.client.Common.sockLog(tempSock2, "Closing unused tempSocket. pending_data_socks size=" + pending_data_socks.size());
                                tempSock2.close();
                            }
                        } else if (Integer.parseInt(new String(pb).trim()) <= 0) {
                            throw new IOException("DMZ socket failed to start.");
                        }
                        if (!reverse) {
                            Log.log("SERVER", 2, "GET:SOCKET:Request for socket with prefered port:" + new String(pb).trim());
                            Properties server_item_temp = null;
                            int x = 0;
                            while (x < ServerStatus.VG("server_list").size()) {
                                Properties si = (Properties)ServerStatus.VG("server_list").elementAt(x);
                                if (si.getProperty("serverType").startsWith("HTTP") && si.getProperty("port").equals(String.valueOf(Integer.parseInt(new String(pb).trim())))) {
                                    server_item_temp = si;
                                    break;
                                }
                                ++x;
                            }
                            if (server_item_temp == null) {
                                Log.log("SERVER", 2, "GET:SOCKET:Prefered port not found...finding first HTTP(s) item to use...");
                                x = 0;
                                while (x < ServerStatus.VG("server_list").size()) {
                                    server_item_temp = (Properties)ServerStatus.VG("server_list").elementAt(x);
                                    if (server_item_temp.getProperty("serverType").equals("HTTP") || server_item_temp.getProperty("serverType").equals("HTTPS")) break;
                                    ++x;
                                }
                            }
                            if (server_item_temp.getProperty("https_redirect", "false").equalsIgnoreCase("true")) {
                                server_item_temp = (Properties)server_item_temp.clone();
                                server_item_temp.put("https_redirect", "false");
                                Log.log("DMZ", 0, "You must turn off HTTPS redirect on your first HTTP port to prevent DMZ issues.");
                            }
                            tempSock2.setSoTimeout(0);
                            com.crushftp.client.Common.sockLog(tempSock2, "tempSock2 starting protocol handling");
                            Thread.currentThread().setName("DMZ using data sock:" + new String(pb).trim());
                            QuickConnect quicky = new QuickConnect(DMZServerCommon.this.thisObj, DMZServerCommon.this.listen_port, tempSock2, DMZServerCommon.this.the_ip, String.valueOf(DMZServerCommon.this.listen_ip) + "_" + DMZServerCommon.this.listen_port, server_item_temp, "");
                            if (!Worker.startWorker(quicky, String.valueOf(DMZServerCommon.this.listen_ip) + "_" + DMZServerCommon.this.listen_port + " --> " + DMZServerCommon.this.the_ip)) {
                                com.crushftp.client.Common.sockLog(tempSock2, "tempSock2 no workers. pending_data_socks size=" + pending_data_socks.size());
                                tempSock2.close();
                                quicky = null;
                                29 var7_11 = this;
                                synchronized (var7_11) {
                                    --DMZServerCommon.this.connected_users;
                                    if (DMZServerCommon.this.connected_users < 0) {
                                        DMZServerCommon.this.connected_users = 0;
                                    }
                                }
                            }
                        }
                        ServerStatus.siPUT("thread_pool_available", String.valueOf(Worker.availableWorkers.size()));
                        ServerStatus.siPUT("thread_pool_busy", String.valueOf(Worker.busyWorkers.size()));
                    }
                    catch (Exception e) {
                        com.crushftp.client.Common.sockLog(tempSock2, "tempSock2 IOException:" + e);
                        if (("" + e).indexOf("invalid prefered port") >= 0) {
                            Log.log("DMZ", 3, "Closing expired socket." + e);
                        } else {
                            Log.log("DMZ", 2, e);
                        }
                        com.crushftp.client.Common.sockLog(tempSock2, "tempSock2 closing.  pending_data_socks size=" + pending_data_socks.size());
                        if (pending_data_socks.size() < 1 && Log.log("DMZ", 2, "")) {
                            com.crushftp.client.Common.sockLog(tempSock2, com.crushftp.client.Common.dumpStack("2:Out of DMZ sockets...:" + pending_data_socks.size() + ":" + e));
                        }
                        try {
                            if (tempSock2 != null) {
                                tempSock2.close();
                            }
                            pending_data_socks.remove(tempSock2);
                        }
                        catch (IOException iOException) {
                            // empty catch block
                        }
                    }
                }
            }, "DMZ pending data sock");
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void startSocketConnectors() throws Exception {
        Worker.startWorker(this.getSocketConnector(), "DMZ:SocketConnector:" + this.server_item.getProperty("server_item_name"));
        Worker.startWorker(this.getSocketReceiver(), "DMZ:SocketReceiver:" + this.server_item.getProperty("server_item_name"));
        Worker.startWorker(this.getResponseProcessor(), "DMZ:ResponseProcessor:" + this.server_item.getProperty("server_item_name"));
        Worker.startWorker(this.getLoggingSocket(), "DMZ:LoggingSocket:" + this.server_item.getProperty("server_item_name"));
    }

    public static void sendFileToMemory(String path, String dmz_instance) throws Exception {
        if (path != null && !path.equals("") && new File_S(path).exists()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            com.crushftp.client.Common.streamCopier(null, null, new FileInputStream(new File_S(path)), baos, false, true, true);
            Properties pp = new Properties();
            pp.put("bytes", baos.toByteArray());
            Properties system_prop = new Properties();
            system_prop.put("key", "crushftp.keystores." + path.toUpperCase().replace('\\', '/'));
            system_prop.put("val", pp);
            DMZServerCommon.sendCommand(dmz_instance, system_prop, "PUT:SYSTEM.PROPERTIES", "");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void updateStatus() {
        Object object = updateServerStatuses;
        synchronized (object) {
            if (!this.started) {
                return;
            }
            this.updateStatusInit();
            String msg = this.socket_created ? "running" : "stopped";
            this.server_item.put("display", String.valueOf(this.busyMessage.equals("") ? "" : "(" + this.busyMessage + ")  ") + LOC.G("dmz://$0:($1)/  ($2) is $3, $4 messages received, $5 messages sent.", this.server_item.getProperty("ip"), this.server_item.getProperty("port"), this.server_item.getProperty("server_item_name", ""), msg, String.valueOf(this.messages_received), String.valueOf(this.messages_sent)));
        }
    }

    public static void createInOutSockRef(Properties ref, Socket sock) throws IOException {
        Properties in_out = new Properties();
        in_out.put("in", new DataInputStream(sock.getInputStream()));
        in_out.put("out", new DataOutputStream(sock.getOutputStream()));
        ref.put(sock, in_out);
    }

    public static void closeInOutSockRef(Properties ref, Socket sock) throws IOException {
        Properties in_out = (Properties)ref.remove(sock);
        try {
            ((DataInputStream)in_out.remove("in")).close();
            ((DataOutputStream)in_out.remove("out")).close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        sock.close();
    }

    public static Properties doGetUserv9Fix(Properties server_item_temp, String username, String password, String user_ip, Properties p2) {
        try {
            VFS uVFS;
            Properties user;
            Vector public_keys;
            block26: {
                block25: {
                    public_keys = null;
                    user = UserTools.ut.getUser(server_item_temp.getProperty("linkedServer", ""), username, true);
                    uVFS = null;
                    try {
                        uVFS = UserTools.ut.getVFS(server_item_temp.getProperty("linkedServer", ""), username);
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                        user = null;
                    }
                    if (user == null && System.getProperty("crushftp.webapplication.enabled", "false").equals("false")) break block25;
                    if (!ServerStatus.BG("always_validate_plugins_for_dmz_lookup")) break block26;
                }
                Log.log("SERVER", 2, "GET:SOCKET:Attempting simulated login via plugins...");
                SessionCrush tempSession = new SessionCrush(null, 1, "127.0.0.1", 0, "0.0.0.0", server_item_temp.getProperty("linkedServer", ""), server_item_temp);
                try {
                    tempSession.user_info.put("request", p2);
                    tempSession.user_info.put("no_log_invalid_password", "true");
                    boolean otp_validation = false;
                    if (user != null) {
                        boolean bl = otp_validation = user.getProperty("otp_auth", "").equals("true") && password.indexOf(":") >= 0;
                    }
                    if (!otp_validation) {
                        tempSession.verify_user(username, password, false, false);
                    }
                    if (!otp_validation) {
                        user = tempSession.user;
                    }
                    if (!otp_validation) {
                        uVFS = tempSession.uVFS;
                    }
                }
                catch (Exception e) {
                    Log.log("DMZ", 1, e);
                }
            }
            if (user != null) {
                public_keys = UserTools.buildPublicKeys(username, user, server_item_temp.getProperty("linkedServer", ""));
            }
            if (user != null) {
                DMZServerCommon.sendFileToMemory(user.getProperty("as2EncryptKeystorePath", ""), server_item_temp.getProperty("server_item_name", ""));
                DMZServerCommon.sendFileToMemory(user.getProperty("as2SignKeystorePath", ""), server_item_temp.getProperty("server_item_name", ""));
                UserTools.setupVFSLinking(server_item_temp.getProperty("linkedServer", ""), username, uVFS, user);
                user.remove("filePublicEncryptionKey");
                user.remove("fileEncryptionKey");
                user.remove("fileDecryptionKey");
                if (user.getProperty("otp_auth", "").equals("true")) {
                    Properties otp_tokens = (Properties)ServerStatus.thisObj.server_info.get("otp_tokens");
                    if (ServerStatus.BG("username_uppercase")) {
                        username = username.toUpperCase();
                    }
                    if (ServerStatus.BG("lowercase_usernames")) {
                        username = username.toLowerCase();
                    }
                    Log.log("LOGIN", 1, "DMZ CHALLENGE_OTP : User: " + username + " with Ip: " + user_ip);
                    if (otp_tokens != null && otp_tokens.containsKey(String.valueOf(username) + (user_ip.equals("") ? "127.0.0.1" : user_ip)) && password.indexOf(":") >= 0) {
                        Properties token = (Properties)otp_tokens.get(String.valueOf(username) + (user_ip.equals("") ? "127.0.0.1" : user_ip));
                        if (password.indexOf(":") >= 0) {
                            password = password.substring(password.lastIndexOf(":") + 1);
                        }
                        if (!user.getProperty("twofactor_secret", "").equals("")) {
                            password = "TOTP:" + ServerStatus.thisObj.common_code.decode_pass(user.getProperty("twofactor_secret"));
                        }
                        if (token.getProperty("token", "").equalsIgnoreCase(password)) {
                            Log.log("LOGIN", 1, "DMZ CHALLENGE_OTP : OTP token is valid.");
                            user.put("otp_valid", "true");
                        } else {
                            user.put("otp_valid", "false");
                            Log.log("LOGIN", 1, "DMZ CHALLENGE_OTP : OTP invalid.");
                        }
                    }
                }
                p2.put("public_keys", public_keys);
                p2.put("user", user);
                p2.put("vfs", uVFS.homes);
                Properties internal_server_data = new Properties();
                if (ServerStatus.BG("user_reveal_hostname")) {
                    internal_server_data.put("internal_app_version", ServerStatus.version_info_str);
                }
                if (internal_server_data.size() > 0) {
                    p2.put("internal_server_data", internal_server_data);
                }
            }
            return user;
        }
        catch (Exception e) {
            Log.log("DMZ", 0, e);
            return null;
        }
    }
}

