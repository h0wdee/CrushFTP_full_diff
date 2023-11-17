/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.Common;
import com.crushftp.ssl.sni.SNIReady;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.handlers.SSLKeyManager;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.CustomServer;
import crushftp.server.daemon.DMZServer1;
import crushftp.server.daemon.DMZServer3;
import crushftp.server.daemon.DMZServer4;
import crushftp.server.daemon.DMZServer5;
import crushftp.server.daemon.ServerBeat;
import crushftp.server.daemon.TCPServer;
import crushftp.server.daemon.TFTPServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocketFactory;

public class GenericServer
implements Runnable {
    public Thread thread = null;
    public ServerSocket server_sock = null;
    public int listen_port = 21;
    public boolean socket_created = false;
    Socket sock = null;
    public StringBuffer die_now = new StringBuffer();
    public String listen_ip = "lookup";
    public String the_ip = "";
    public Properties server_item = null;
    String busyMessage = "";
    boolean port_denied = false;
    public int connection_number = 0;
    public int connected_users = 0;
    String startingPropertiesHash = "";
    boolean restart = false;
    boolean started = false;
    static boolean warned = false;
    public static transient Object updateServerStatuses = new Object();
    public static Vector serverPorts = new Vector();
    boolean sni_enabled = false;
    boolean needClientAuth = false;
    String keystore = null;
    String certPass = null;
    String keystorePass = null;
    SSLContext ssl_context = null;
    SSLSocketFactory factory = null;

    public GenericServer(Properties server_item) {
        this.server_item = server_item;
    }

    @Override
    public void run() {
    }

    public static GenericServer buildServer(Properties server_item) {
        if (server_item.getProperty("serverType", "").equalsIgnoreCase("CUSTOM")) {
            return new CustomServer(server_item);
        }
        if (server_item.getProperty("serverType", "").equalsIgnoreCase("SERVERBEAT")) {
            return new ServerBeat(server_item);
        }
        if (server_item.getProperty("serverType", "").toUpperCase().indexOf("DMZ") >= 0 && server_item.getProperty("tunneled", "false").equals("true")) {
            return new DMZServer3(server_item);
        }
        if (server_item.getProperty("serverType", "").toUpperCase().indexOf("DMZ") >= 0 && server_item.getProperty("dmz_version", "").equals("4")) {
            return new DMZServer4(server_item);
        }
        if (server_item.getProperty("serverType", "").toUpperCase().indexOf("DMZ") >= 0 && server_item.getProperty("dmz_version", "").equals("5")) {
            return new DMZServer5(server_item);
        }
        if (server_item.getProperty("serverType", "").toUpperCase().indexOf("DMZ") >= 0) {
            return new DMZServer1(server_item);
        }
        if (server_item.getProperty("serverType", "").toUpperCase().indexOf("TFTP") >= 0) {
            return new TFTPServer(server_item);
        }
        return new TCPServer(server_item);
    }

    public void init() {
        this.thread = Thread.currentThread();
        this.listen_port = Integer.parseInt(this.server_item.getProperty("port"));
        this.listen_ip = this.server_item.getProperty("ip");
        serverPorts.addElement(String.valueOf(this.listen_port));
        this.the_ip = this.listen_ip;
        this.startingPropertiesHash = GenericServer.getPropertiesHash((Properties)this.server_item.clone());
        this.started = true;
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void getSocket() {
        try {
            block10: while (true) {
                block45: {
                    if (this.socket_created || this.die_now.length() != 0) {
                        if (this.socket_created == false) return;
                        if (this.die_now.length() != 0) return;
                        if (this.listen_ip.equals("lookup") == false) return;
                        this.the_ip = ServerStatus.SG("discovered_ip");
                        return;
                    }
                    try {
                        block46: {
                            if (this.server_item.getProperty("enabled", "true").equals("false")) {
                                throw new Exception("Port is set to disabled in preferences.");
                            }
                            allowed = false;
                            allowed_ips = System.getProperty("crushftp.server.ips", "*").split(",");
                            x = 0;
                            while (true) {
                                if (x >= allowed_ips.length) {
                                    if (allowed) break;
                                    throw new IOException("Server ip not allowed:" + System.getProperty("crushftp.server.ips", "*"));
                                }
                                if (!allowed_ips[x].trim().equals("") && Common.do_search(allowed_ips[x].trim(), this.listen_ip, false, 0)) {
                                    allowed = true;
                                }
                                ++x;
                            }
                            if (!this.server_item.getProperty("serverType", "false").toUpperCase().equals("FTPS") && !this.server_item.getProperty("serverType", "FTP").toUpperCase().equals("HTTPS") && !this.server_item.getProperty("serverType", "FTP").toUpperCase().equals("PORTFORWARDS")) break block46;
                            this.busyMessage = LOC.G("SSL Cert Error");
                            this.keystore = SSLKeyManager.loadKeyStoreToMemory(ServerStatus.SG("cert_path"));
                            this.certPass = ServerStatus.SG("globalKeystoreCertPass");
                            this.keystorePass = ServerStatus.SG("globalKeystorePass");
                            this.needClientAuth = ServerStatus.BG("needClientAuth");
                            if (!this.server_item.getProperty("customKeystore", "").equals("")) {
                                this.keystore = SSLKeyManager.loadKeyStoreToMemory(this.server_item.getProperty("customKeystore"));
                                this.certPass = this.server_item.getProperty("customKeystoreCertPass", "");
                                this.keystorePass = this.server_item.getProperty("customKeystorePass", "");
                            }
                            if (!this.server_item.getProperty("needClientAuth", "false").equals("false")) {
                                this.needClientAuth = this.server_item.getProperty("needClientAuth", "").equals("true");
                            }
                            if (this.server_item.getProperty("sni_enabled", "false").equals("true")) {
                                try {
                                    new SNIReady().test();
                                    this.sni_enabled = true;
                                }
                                catch (Throwable x) {
                                    // empty catch block
                                }
                            }
                            v0 = allowBuiltin = Common.dmz_mode == false || this.keystore.equalsIgnoreCase("builtin") != false;
                            if (this.ssl_context == null) {
                                this.ssl_context = ServerStatus.thisObj.common_code.getSSLContext(this.keystore, String.valueOf(this.keystore) + "_trust", this.keystorePass, this.certPass, "TLS", this.needClientAuth, allowBuiltin);
                            }
                            this.server_sock = this.listen_ip.equals("lookup") != false ? ServerStatus.thisObj.common_code.getServerSocket(this.listen_port, null, this.keystore, this.keystorePass, this.certPass, ServerStatus.SG("disabled_ciphers"), this.needClientAuth, 1000, true, false, this.ssl_context) : ServerStatus.thisObj.common_code.getServerSocket(this.listen_port, this.listen_ip, this.keystore, this.keystorePass, this.certPass, ServerStatus.SG("disabled_ciphers"), this.needClientAuth, 1000, true, false, this.ssl_context);
                            ciphers = ((SSLServerSocket)this.server_sock).getSupportedCipherSuites();
                            cipherStr = "";
                            x = 0;
                            if (true) ** GOTO lbl124
                        }
                        if (this.server_item.getProperty("serverType", "false").toUpperCase().indexOf("DMZ") >= 0) {
                            this.server_sock = new ServerSocket(0);
                            this.server_sock.close();
                        } else {
                            this.server_sock = this.listen_ip.equals("lookup") != false ? new ServerSocket(this.listen_port, 1000, null) : new ServerSocket(this.listen_port, 1000, InetAddress.getByName(this.listen_ip));
                        }
lbl56:
                        // 3 sources

                        while (true) {
                            this.socket_created = true;
                            ServerStatus.thisObj.server_started(this.listen_ip, this.listen_port);
                            this.updateStatus();
                            continue block10;
                            break;
                        }
                    }
                    catch (Exception ee) {
                        this.busyMessage = String.valueOf(this.busyMessage) + ":" + ee.toString();
                        sleepAmount = 30000;
                        if (this.busyMessage.indexOf("disabled") < 0) {
                            Log.log("SERVER", 2, ee);
                        }
                        if (this.busyMessage.toLowerCase().indexOf("invalid") > 0) {
                            Log.log("SERVER", 1, ee);
                        }
                        if (ee.toString().indexOf("Permission denied") >= 0) {
                            this.busyMessage = LOC.G("Port $0 is reserved since its below 1024.  Authenticate as root to fix.", String.valueOf(this.listen_port));
                        } else if (ee.toString().indexOf("assign requested") >= 0) {
                            this.busyMessage = LOC.G("The IP specified ($0) is invalid.  This machine is not using that IP.  Please update in preferences.", this.listen_ip);
                        } else if (ee.toString().indexOf("Address already in use") >= 0) {
                            this.busyMessage = LOC.G("Port $0 is already in use by another process.", String.valueOf(this.listen_port));
                        }
                        if (!GenericServer.warned) {
                            GenericServer.warned = true;
                            Log.log("SERVER", 0, this.busyMessage);
                        }
                        data = "";
                        if (!this.busyMessage.equals("")) break block45;
                        try {
                            testSock = new Socket("127.0.0.1", this.listen_port);
                            in = new BufferedReader(new InputStreamReader(testSock.getInputStream()));
                            data = " ";
                            while (true) {
                                if (data.indexOf("220 ") >= 0 || data.indexOf("null") >= 0) {
                                    testSock.getOutputStream().write("QUIT\r\n".getBytes());
                                    in.close();
                                    testSock.close();
                                }
                                data = String.valueOf(data) + in.readLine();
                            }
                        }
                        catch (Exception eee) {
                            Thread.sleep(1000L);
                        }
                    }
                    data = data.toUpperCase();
                    this.busyMessage = String.valueOf(this.listen_ip) + ":" + this.listen_port + " - " + LOC.G("Port in use by some other server : $0", ee.toString());
                    if (!this.busyMessage.equals("")) {
                        Log.log("SERVER", 0, this.busyMessage);
                    }
                }
                if (!this.busyMessage.equals("")) {
                    Thread.sleep(sleepAmount);
                    this.busyMessage = "";
                    continue;
                }
                loopNum = 30;
                while (true) {
                    if (loopNum < 0) {
                        this.busyMessage = "";
                        continue block10;
                    }
                    this.busyMessage = LOC.G("Port $0 in use! Retrying $1 secs...", String.valueOf(this.listen_port), String.valueOf(loopNum--));
                    Thread.sleep(1000L);
                }
                break;
            }
        }
        catch (InterruptedException var1_3) {
            // empty catch block
        }
        return;
        do {
            if (x > 0) {
                cipherStr = String.valueOf(cipherStr) + ",";
            }
            cipherStr = String.valueOf(cipherStr) + ciphers[x].toUpperCase();
            ++x;
lbl124:
            // 2 sources

        } while (x < ciphers.length);
        ServerStatus.siPUT("ciphers", cipherStr);
        ciphers = ((SSLServerSocket)this.server_sock).getEnabledCipherSuites();
        cipherStr = "";
        x = 0;
        while (x < ciphers.length) {
            if (x > 0) {
                cipherStr = String.valueOf(cipherStr) + ",";
            }
            cipherStr = String.valueOf(cipherStr) + ciphers[x].toUpperCase();
            ++x;
        }
        if (!cipherStr.equals("")) {
            ServerStatus.siPUT("enabled_ciphers", cipherStr);
        }
        if (this.sni_enabled || (this.server_item.getProperty("proxy_header", "false").equals("true") || this.server_item.getProperty("proxy_header_v2", "false").equals("true")) && (this.server_item.getProperty("serverType", "").toUpperCase().equals("FTPS") || this.server_item.getProperty("serverType", "").toUpperCase().equals("HTTPS"))) {
            if (this.server_sock != null) {
                this.server_sock.close();
            }
            this.server_sock = this.listen_ip.equals("lookup") != false ? new ServerSocket(this.listen_port, 1000, null) : new ServerSocket(this.listen_port, 1000, InetAddress.getByName(this.listen_ip));
        }
        if (System.getProperty("crushftp.letsencrypt.acme4j_alpn", "false").equals("true")) {
            System.getProperties().put("crushftp.letsencrypt.acme4j_alpn", "false");
            try {
                Log.log("SERVER", 1, "ALPN : Is activated.");
                method = SSLServerSocket.class.getDeclaredMethod("getSSLParameters", null);
                Log.log("SERVER", 2, "ALPN :getSSLParameters");
                sslp = method.invoke(this.server_sock, new Object[0]);
                acm4j_protocol = new String[]{"acme-tls/1"};
                method2 = sslp.getClass().getDeclaredMethod("setApplicationProtocols", new Class[]{String[].class});
                method2.invoke(sslp, new Object[]{acm4j_protocol});
                Log.log("SERVER", 2, "ALPN :setApplicationProtocols");
                method3 = SSLServerSocket.class.getDeclaredMethod("setSSLParameters", new Class[]{sslp.getClass()});
                method3.invoke(this.server_sock, new Object[]{sslp});
                Log.log("SERVER", 2, "ALPN :setSSLParameters");
            }
            catch (Exception e) {
                Log.log("SERVER", 1, "ALPN :" + e);
            }
        }
        this.busyMessage = "";
        ** while (true)
    }

    public void updateStatus() {
    }

    public void updateStatusInit() {
        if (!this.started) {
            return;
        }
        String hash2 = GenericServer.getPropertiesHash((Properties)this.server_item.clone());
        if (!this.startingPropertiesHash.equals(hash2) && !this.startingPropertiesHash.equals("")) {
            this.startingPropertiesHash = crushftp.handlers.Common.makeBoundary();
            this.restart = true;
            this.die_now.append(System.currentTimeMillis());
            try {
                this.server_sock.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        boolean found = false;
        int x = 0;
        while (x < ((Vector)ServerStatus.server_settings.get("server_list")).size()) {
            Properties pp = (Properties)((Vector)ServerStatus.server_settings.get("server_list")).elementAt(x);
            if (pp.getProperty("ip").equals(this.server_item.getProperty("ip")) && pp.getProperty("port").equals(this.server_item.getProperty("port"))) {
                found = true;
            }
            ++x;
        }
        if (!found) {
            this.die_now.append(System.currentTimeMillis());
            try {
                this.server_sock.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        ServerStatus.thisObj.server_info.put("server_list", ServerStatus.server_settings.get("server_list"));
        this.server_item.put("running", String.valueOf(this.socket_created));
        this.server_item.put("connected_users", String.valueOf(this.connected_users));
        this.server_item.put("connection_number", String.valueOf(this.connection_number));
        this.server_item.put("busyMessage", this.busyMessage);
    }

    public static String getPropertiesHash(Properties p) {
        p.remove("display");
        p.remove("connected_users");
        p.remove("connection_number");
        p.remove("running");
        p.remove("busyMessage");
        p.remove("ssh_local_port");
        p.remove("current_pasv_port");
        p.remove("require_secure");
        p.remove("server_ip");
        p.remove("allow_webdav");
        p.remove("linkedServer");
        p.remove("require_encryption");
        p.remove("pasv_ports");
        p.remove("explicit_ssl");
        p.remove("explicit_tls");
        p.remove("ftp_aware_router");
        p.remove("https_redirect");
        p.remove("commandDelayInterval");
        Enumeration<Object> keys = p.keys();
        Vector<String> v = new Vector<String>();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            v.addElement(key);
        }
        Object[] a = v.toArray();
        Arrays.sort(a);
        String s = "";
        int x = 0;
        while (x < a.length) {
            Object o = p.get(a[x]);
            if (o instanceof String) {
                s = String.valueOf(s) + "," + a[x] + o.toString();
            }
            ++x;
        }
        if (p.getProperty("serverType", "false").toUpperCase().equals("FTPS") || p.getProperty("serverType", "FTP").toUpperCase().equals("HTTPS")) {
            String cert_path = "";
            if (ServerStatus.server_settings.get("cert_path") instanceof String) {
                cert_path = ServerStatus.SG("cert_path");
            }
            if (ServerStatus.server_settings.get("cert_path") instanceof Properties) {
                cert_path = ((Properties)ServerStatus.server_settings.get("cert_path")).getProperty("url", "");
            }
            s = String.valueOf(s) + "," + cert_path + ServerStatus.SG("globalKeystoreCertPass") + ServerStatus.SG("globalKeystorePass");
        }
        s = String.valueOf(s) + ";";
        return s;
    }
}

