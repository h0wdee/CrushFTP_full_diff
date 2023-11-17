/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lw.bouncycastle.util.Arrays
 */
package crushftp.server.daemon;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.Worker;
import com.crushftp.ssl.sni.SNITool;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.handlers.PortMapper;
import crushftp.handlers.SSLKeyManager;
import crushftp.server.QuickConnect;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.GenericServer;
import crushftp.server.ssh.SSHDaemon;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import lw.bouncycastle.util.Arrays;

public class TCPServer
extends GenericServer {
    SSHDaemon sshd;
    PortMapper portmapper = new PortMapper();
    Vector sockets = new Vector();
    TCPServer thisObj = this;
    SSLSocketFactory factory = null;
    static final byte[] header_master_proxy_v2;

    static {
        byte[] byArray = new byte[12];
        byArray[0] = 13;
        byArray[1] = 10;
        byArray[2] = 13;
        byArray[3] = 10;
        byArray[5] = 13;
        byArray[6] = 10;
        byArray[7] = 81;
        byArray[8] = 85;
        byArray[9] = 73;
        byArray[10] = 84;
        byArray[11] = 10;
        header_master_proxy_v2 = byArray;
    }

    public TCPServer(Properties server_item) {
        super(server_item);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        block22: {
            this.init();
            try {
                this.getSocket();
                if (this.socket_created && this.die_now.length() == 0 && this.server_item.getProperty("serverType", "").toUpperCase().equals("SFTP")) {
                    this.sshd = new SSHDaemon(this.server_item);
                    this.sshd.startup();
                }
                this.server_sock.setSoTimeout(30000);
                long last_map = 0L;
                while (this.socket_created && this.die_now.length() == 0) {
                    block21: {
                        this.busyMessage = "";
                        if (this.server_item.getProperty("configure_external", "false").equals("true") && System.currentTimeMillis() - last_map > 3300000L) {
                            boolean mapped = false;
                            try {
                                mapped = this.portmapper.mapPort(this.listen_ip, Integer.parseInt(this.server_item.getProperty("port")), 3600000);
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                            }
                            this.server_item.put("external_mapped", String.valueOf(mapped));
                            last_map = System.currentTimeMillis();
                        }
                        try {
                            this.sock = this.server_sock.accept();
                        }
                        catch (SocketTimeoutException e) {
                            continue;
                        }
                        this.sockets.addElement(this.sock);
                        Runnable sr = new Runnable(){

                            /*
                             * WARNING - Removed try catching itself - possible behaviour change.
                             */
                            @Override
                            public void run() {
                                String proxied_ip = "";
                                Socket sock2 = (Socket)TCPServer.this.sockets.remove(0);
                                if (TCPServer.this.server_item.getProperty("proxy_header", "false").equals("true")) {
                                    proxied_ip = TCPServer.this.readProxyIP(sock2, TCPServer.this.thisObj);
                                }
                                if (TCPServer.this.server_item.getProperty("proxy_header_v2", "false").equals("true")) {
                                    proxied_ip = TCPServer.this.readProxyIPV2(sock2, TCPServer.this.thisObj);
                                }
                                if ((TCPServer.this.server_item.getProperty("proxy_header", "false").equals("true") || TCPServer.this.server_item.getProperty("proxy_header_v2", "false").equals("true")) && (TCPServer.this.server_item.getProperty("serverType", "").toUpperCase().equals("FTPS") || TCPServer.this.server_item.getProperty("serverType", "").toUpperCase().equals("HTTPS")) && !TCPServer.this.sni_enabled) {
                                    try {
                                        if (TCPServer.this.factory == null) {
                                            TCPServer.this.factory = ServerStatus.thisObj.common_code.getSSLContext(TCPServer.this.keystore, String.valueOf(TCPServer.this.keystore) + "_trust", TCPServer.this.keystorePass, TCPServer.this.certPass, "TLS", TCPServer.this.needClientAuth, true).getSocketFactory();
                                        }
                                        sock2 = (SSLSocket)TCPServer.this.factory.createSocket(sock2, sock2.getInetAddress().getHostAddress(), sock2.getPort(), true);
                                        crushftp.handlers.Common.configureSSLTLSSocket(sock2);
                                        crushftp.handlers.Common.setEnabledCiphers(ServerStatus.SG("disabled_ciphers"), (SSLSocket)sock2, null);
                                        ((SSLSocket)sock2).setNeedClientAuth(TCPServer.this.needClientAuth);
                                        ((SSLSocket)sock2).setUseClientMode(false);
                                    }
                                    catch (Exception e) {
                                        Log.log("SERVER", 0, e);
                                    }
                                }
                                StringBuffer sni_keystore_used = null;
                                if (TCPServer.this.sni_enabled) {
                                    if (TCPServer.this.server_item.getProperty("serverType", "").toUpperCase().equals("FTPS")) {
                                        sni_keystore_used = new StringBuffer();
                                    }
                                    sock2 = TCPServer.doSni(sock2, TCPServer.this.keystore, TCPServer.this.keystorePass, TCPServer.this.certPass, TCPServer.this.needClientAuth, sni_keystore_used);
                                }
                                if (TCPServer.this.sshd != null) {
                                    TCPServer.this.server_item.put("ssh_local_port", String.valueOf(TCPServer.this.sshd.localSSHPort));
                                }
                                TCPServer tCPServer = TCPServer.this.thisObj;
                                synchronized (tCPServer) {
                                    ++TCPServer.this.connected_users;
                                    if (TCPServer.this.connected_users < 0) {
                                        TCPServer.this.connected_users = 1;
                                    }
                                }
                                ++TCPServer.this.connection_number;
                                if (TCPServer.this.listen_ip.equals("lookup") || TCPServer.this.listen_ip.equals("manual")) {
                                    TCPServer.this.the_ip = ServerStatus.SG("discovered_ip");
                                }
                                if (!(!TCPServer.this.server_item.getProperty("serverType", "").toUpperCase().equals("FTP") && !TCPServer.this.server_item.getProperty("serverType", "").toUpperCase().equals("FTPS") || TCPServer.this.server_item.getProperty("server_ip", "").trim().equals("") || TCPServer.this.server_item.getProperty("server_ip", "").trim().equals("auto") || TCPServer.this.server_item.getProperty("server_ip").trim().charAt(0) <= '9' && TCPServer.this.server_item.getProperty("server_ip", "").indexOf(",") < 0 || TCPServer.this.server_item.getProperty("server_ip", "").trim().equals("lookup"))) {
                                    TCPServer.this.the_ip = TCPServer.this.server_item.getProperty("server_ip", "");
                                }
                                TCPServer.this.updateStatus();
                                QuickConnect qconnect = new QuickConnect(TCPServer.this.thisObj, TCPServer.this.listen_port, sock2, TCPServer.this.the_ip, String.valueOf(TCPServer.this.listen_ip) + "_" + TCPServer.this.listen_port, TCPServer.this.server_item, proxied_ip);
                                if (sni_keystore_used != null) {
                                    qconnect.sni_keystore_used = sni_keystore_used;
                                }
                                qconnect.run();
                            }
                        };
                        try {
                            if (Worker.startWorker(sr, String.valueOf(this.listen_ip) + "_" + this.listen_port + " --> " + this.the_ip)) break block21;
                            this.sockets.remove(this.sock);
                            this.sock.close();
                            TCPServer e = this.thisObj;
                            synchronized (e) {
                                --this.connected_users;
                                if (this.connected_users < 0) {
                                    this.connected_users = 0;
                                }
                            }
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                        }
                    }
                    ServerStatus.siPUT("thread_pool_available", String.valueOf(Worker.availableWorkers.size()));
                    ServerStatus.siPUT("thread_pool_busy", String.valueOf(Worker.busyWorkers.size()));
                }
            }
            catch (Throwable e) {
                if (e.getMessage() == null || e.getMessage().indexOf("socket closed") < 0 && e.getMessage().indexOf("disabled") < 0) {
                    Log.log("SERVER", 1, e);
                } else {
                    Log.log("SERVER", 3, e);
                }
                if (("" + e).toUpperCase().indexOf("INTERRUPTED") >= 0) break block22;
                try {
                    Properties info = new Properties();
                    info.put("alert_type", "server_port_error");
                    info.put("alert_error", "" + e);
                    info.put("alert_msg", this.server_item.getProperty("display"));
                    ServerStatus.thisObj.runAlerts("server_port_error", info, info, null);
                }
                catch (Exception ee) {
                    Log.log("BAN", 1, ee);
                }
            }
        }
        if (this.sshd != null) {
            this.shutdownSSHDLater();
            this.restart = false;
        }
        this.socket_created = false;
        this.updateStatus();
        this.portmapper.clearAll();
        if (this.restart) {
            this.restart = false;
            this.die_now = new StringBuffer();
            new Thread(this.thisObj).start();
        }
    }

    public void shutdownSSHDLater() {
        final TCPServer running_instance = this.thisObj;
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    while (running_instance.connected_users > 0) {
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                    }
                    running_instance.sshd.stop();
                }
            });
        }
        catch (IOException iOException) {
            // empty catch block
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
            if (this.socket_created) {
                if (this.server_item.getProperty("configure_external", "false").equals("true")) {
                    this.server_item.put("display", String.valueOf(this.busyMessage.equals("") ? "" : "(" + this.busyMessage + ")  ") + LOC.G("$0 is running, $1 users connected. Port Forwarded : $2, Connections Processed : $3", ServerStatus.thisObj.common_code.setServerStatus(this.server_item, this.the_ip).trim(), String.valueOf(this.connected_users), this.server_item.getProperty("external_mapped", "false"), String.valueOf(this.connection_number)));
                } else {
                    this.server_item.put("display", String.valueOf(this.busyMessage.equals("") ? "" : "(" + this.busyMessage + ")  ") + LOC.G("$0 is running, $1 users connected. Connections Processed : $2", ServerStatus.thisObj.common_code.setServerStatus(this.server_item, this.the_ip).trim(), String.valueOf(this.connected_users), String.valueOf(this.connection_number)));
                }
            } else {
                this.server_item.put("display", String.valueOf(this.busyMessage.equals("") ? "" : "(" + this.busyMessage + ")  ") + LOC.G("$0 is stopped, $1 users still connected.  Connections Processed : $2", ServerStatus.thisObj.common_code.setServerStatus(this.server_item, this.the_ip).trim(), String.valueOf(this.connected_users), String.valueOf(this.connection_number)));
            }
        }
    }

    public static Socket doSni(Socket sock2, String keystore, String keystorePass, String certPass, boolean needClientAuth, StringBuffer keystore_used) {
        try {
            sock2.setSoTimeout(5000);
            Properties result = SNITool.check(sock2);
            sock2.setSoTimeout(0);
            if (result.containsKey("error")) {
                throw (Exception)result.remove("error");
            }
            Vector names = (Vector)result.get("names");
            Log.log("SERVER", 2, "" + names);
            String keystore2 = keystore;
            ByteArrayInputStream bais = (ByteArrayInputStream)result.remove("buffer");
            boolean found = false;
            int x = 0;
            while (x < names.size()) {
                String s = names.elementAt(x).toString();
                String type = s.split(":")[0];
                String host = s.split(":")[1].trim().toLowerCase();
                if (type.equals("0")) {
                    host = Common.dots(host);
                    File_S f = new File_S(keystore);
                    if ((f = new File_S(String.valueOf(f.getParentFile().getAbsolutePath()) + "/" + host + "_" + f.getName())).exists()) {
                        keystore2 = f.getPath();
                        if (keystore_used != null) {
                            keystore_used.append(keystore2);
                        }
                        Log.log("SERVER", 1, "Using keystore " + keystore2 + " for connection:" + sock2);
                        found = true;
                        break;
                    }
                }
                ++x;
            }
            String alias = null;
            if (!found) {
                int x2 = 0;
                while (x2 < names.size() && alias == null) {
                    String s = names.elementAt(x2).toString();
                    String type = s.split(":")[0];
                    String host = s.split(":")[1].trim().toLowerCase();
                    if (type.equals("0")) {
                        Vector aliases = SSLKeyManager.list(keystore2, Common.encryptDecrypt(keystorePass, false));
                        int xx = 0;
                        while (xx < aliases.size() && alias == null) {
                            Properties p = (Properties)aliases.elementAt(xx);
                            if (p.getProperty("private", "").equals("true")) {
                                String tmp_alias = p.getProperty("alias", "");
                                if (host.equalsIgnoreCase(tmp_alias)) {
                                    alias = tmp_alias;
                                    if (keystore_used != null) {
                                        keystore_used.append(keystore2);
                                        keystore_used.append(";!!!keystore_used!!!;");
                                        keystore_used.append(alias);
                                    }
                                } else if (tmp_alias.indexOf("*") >= 0 && Common.do_search(tmp_alias, host, false, 0)) {
                                    alias = tmp_alias;
                                    if (keystore_used != null) {
                                        keystore_used.append(keystore2);
                                        keystore_used.append(";!!!keystore_used!!!;");
                                        keystore_used.append(alias);
                                    }
                                }
                            }
                            ++xx;
                        }
                    }
                    ++x2;
                }
            }
            SSLSocketFactory factory = ServerStatus.thisObj.common_code.getSSLContext(keystore2, String.valueOf(keystore2) + "_trust", keystorePass, certPass, keystorePass, certPass, "TLS", needClientAuth, true, true, alias).getSocketFactory();
            sock2 = SNITool.makeSocket(sock2, factory, bais, ServerStatus.SG("disabled_ciphers"));
            crushftp.handlers.Common.configureSSLTLSSocket(sock2);
            crushftp.handlers.Common.setEnabledCiphers(ServerStatus.SG("disabled_ciphers"), (SSLSocket)sock2, null);
            ((SSLSocket)sock2).setNeedClientAuth(needClientAuth);
        }
        catch (Exception e) {
            Log.log("SERVER", 1, e);
            try {
                sock2.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return sock2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String readProxyIP(Socket sock, GenericServer server) {
        try {
            InputStream in = sock.getInputStream();
            String line = "";
            byte[] b = new byte[1];
            int read = 1;
            while (!line.endsWith("\r\n") && read > 0) {
                read = in.read(b);
                if (read <= 0) continue;
                line = String.valueOf(line) + new String(b);
            }
            return line.split(" ")[2].trim();
        }
        catch (Exception e) {
            Log.log("SERVER", 2, e);
            GenericServer genericServer = server;
            synchronized (genericServer) {
                --server.connected_users;
                if (server.connected_users < 0) {
                    server.connected_users = 0;
                }
            }
            server.updateStatus();
            return null;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public String readProxyIPV2(Socket sock, GenericServer server) {
        try {
            InputStream in = sock.getInputStream();
            byte[] b_header = new byte[1];
            int loc = 0;
            while (loc < header_master_proxy_v2.length) {
                in.read(b_header);
                if (b_header[0] == header_master_proxy_v2[loc++]) continue;
                throw new Exception("Invalid proxy protocol v2 header");
            }
            byte[] b = new byte[1];
            in.read(b);
            int protocol_version = b[0] >>> 4 & 0xF;
            int command = b[0] & 0xF;
            in.read(b);
            int address_family = b[0] >>> 4 & 0xF;
            int transport_protocol = b[0] & 0xF;
            int i15 = in.read();
            int i16 = in.read();
            int total_len = (i15 << 8) + i16;
            byte[] address_info = new byte[total_len];
            if (in.read(address_info) < address_info.length) {
                throw new Exception("Proxy header read of address_info failed due to insufficient bytes.");
            }
            if (protocol_version != 2) {
                throw new Exception("Proxy protocol version did not indicate v2:" + protocol_version);
            }
            if (command == 0 || address_family == 0) {
                return sock.getInetAddress().getHostAddress();
            }
            if (command == 1) {
                if (transport_protocol > 1) {
                    throw new Exception("Transport protocol not supported:" + transport_protocol);
                }
                if (address_family == 1) {
                    return InetAddress.getByAddress(Arrays.copyOfRange((byte[])address_info, (int)0, (int)4)).getHostAddress();
                }
                if (address_family == 2) {
                    return InetAddress.getByAddress(Arrays.copyOfRange((byte[])address_info, (int)0, (int)16)).getHostAddress();
                }
                throw new Exception("Unsupported address family:" + address_family);
            }
            throw new Exception("Proxy protocol command not understood.");
        }
        catch (Exception e) {
            try {
                sock.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            Log.log("SERVER", 2, e);
            GenericServer genericServer = server;
            synchronized (genericServer) {
                --server.connected_users;
                if (server.connected_users < 0) {
                    server.connected_users = 0;
                }
            }
            server.updateStatus();
            return null;
        }
    }
}

