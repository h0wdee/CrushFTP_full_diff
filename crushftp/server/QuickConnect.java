/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server;

import com.crushftp.client.Common;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.server.ServerSessionFTP;
import crushftp.server.ServerSessionHTTP;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.DMZServer1;
import crushftp.server.daemon.DMZServer3;
import crushftp.server.daemon.DMZServerCommon;
import crushftp.server.daemon.GenericServer;
import crushftp.server.ssh.SSHSocket;
import java.awt.Toolkit;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class QuickConnect
implements Runnable {
    public int listen_port = 21;
    Socket sock;
    GenericServer server = null;
    String listen_ip = "127.0.0.1";
    String listen_ip_port = "lookup_21";
    Properties server_item = null;
    String proxied_ip = "";
    public StringBuffer sni_keystore_used = null;
    public static transient Object syncUserNumbers = new Object();
    public static Properties ip_cache = new Properties();
    public static Properties connected_ips = new Properties();

    public QuickConnect(GenericServer server, int listen_port, Socket sock, String listen_ip, String listen_ip_port, Properties server_item, String proxied_ip) {
        this.listen_port = listen_port;
        this.sock = sock;
        this.server = server;
        this.listen_ip = listen_ip;
        this.listen_ip_port = listen_ip_port;
        this.server_item = server_item;
        this.proxied_ip = proxied_ip;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void remove_ip_count(String ip) {
        Properties properties = connected_ips;
        synchronized (properties) {
            int i = Integer.parseInt(connected_ips.getProperty(ip, "0")) - 1;
            if (i == 0) {
                connected_ips.remove(ip);
            } else {
                connected_ips.put(ip, String.valueOf(i));
            }
        }
        ServerStatus.thisObj.server_info.put("connected_unique_ips", String.valueOf(connected_ips.size()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        try {
            OutputStream out;
            String ip;
            block88: {
                int i;
                block93: {
                    block91: {
                        block92: {
                            block90: {
                                block89: {
                                    Thread.currentThread().setName("0.0-" + new Date());
                                    ip = this.sock.getInetAddress().getHostAddress();
                                    Thread.currentThread().setName("0.1-" + ip);
                                    if (this.proxied_ip == null) {
                                        return;
                                    }
                                    if (!this.proxied_ip.equals("")) {
                                        ip = this.proxied_ip;
                                    }
                                    Thread.currentThread().setName("0.2-" + ip);
                                    boolean max_single_ip = false;
                                    int connected_ip = 0;
                                    if (!(this.server instanceof DMZServer1 || this.server instanceof DMZServer3 || this.server instanceof DMZServerCommon)) {
                                        Properties properties = connected_ips;
                                        synchronized (properties) {
                                            connected_ip = Integer.parseInt(connected_ips.getProperty(ip, "0")) + 1;
                                            connected_ips.put(ip, String.valueOf(connected_ip));
                                            if ((long)connected_ip > ServerStatus.LG("max_connection_single_ip")) {
                                                max_single_ip = true;
                                            }
                                        }
                                        ServerStatus.thisObj.server_info.put("connected_unique_ips", String.valueOf(connected_ips.size()));
                                    }
                                    if (max_single_ip) {
                                        try {
                                            ServerStatus.thisObj.append_log(String.valueOf(ServerStatus.thisObj.logDateFormat.format(new Date())) + "|---" + ServerStatus.SG("TOO MANY IP CONNECTIONS TERMINATED/BANNED") + "---:" + ip + " count=" + connected_ip, "DENIAL");
                                        }
                                        catch (Exception exception) {
                                            // empty catch block
                                        }
                                    }
                                    if (!QuickConnect.validate_ip(ip, this.server_item) || max_single_ip) break block88;
                                    Thread.currentThread().setName("0.3-" + ip);
                                    if (ServerStatus.BG("beep_connect")) {
                                        Toolkit.getDefaultToolkit().beep();
                                    }
                                    i = QuickConnect.getUserLoginNum();
                                    Thread.currentThread().setName(String.valueOf(i) + "-" + ip);
                                    if (!this.server_item.getProperty("serverType", "").toUpperCase().equals("SFTP")) break block89;
                                    Properties p = new Properties();
                                    p.put("user_number", String.valueOf(i));
                                    p.put("server_item", this.server_item);
                                    p.put("socket", this.sock);
                                    p.put("user_ip", ip);
                                    p.put("user_port", String.valueOf(this.listen_port));
                                    p.put("listen_ip", this.listen_ip);
                                    p.put("listen_port", String.valueOf(this.listen_port));
                                    p.put("listen_ip_port", this.listen_ip_port);
                                    p.put("connectionTime", String.valueOf(new Date().getTime()));
                                    SSHSocket sshs = new SSHSocket(this.server, p, Integer.parseInt(this.server_item.getProperty("ssh_local_port")));
                                    sshs.run();
                                    if (!(this.server instanceof DMZServer1 || this.server instanceof DMZServer3 || this.server instanceof DMZServerCommon)) {
                                        QuickConnect.remove_ip_count(ip);
                                    }
                                    GenericServer genericServer = this.server;
                                    synchronized (genericServer) {
                                        --this.server.connected_users;
                                        if (this.server.connected_users < 0) {
                                            this.server.connected_users = 0;
                                        }
                                    }
                                    this.server.updateStatus();
                                    return;
                                }
                                if (!this.server_item.getProperty("serverType", "").toUpperCase().equals("HTTP") && !this.server_item.getProperty("serverType", "").toUpperCase().equals("HTTPS")) break block90;
                                ServerSessionHTTP thisSessionHttp = new ServerSessionHTTP(this.sock, i, ip, this.listen_port, this.listen_ip, this.listen_ip_port, this.server_item);
                                thisSessionHttp.give_thread_pointer(Thread.currentThread());
                                thisSessionHttp.run();
                                if (!(this.server instanceof DMZServer1 || this.server instanceof DMZServer3 || this.server instanceof DMZServerCommon)) {
                                    QuickConnect.remove_ip_count(ip);
                                }
                                GenericServer genericServer = this.server;
                                synchronized (genericServer) {
                                    --this.server.connected_users;
                                    if (this.server.connected_users < 0) {
                                        this.server.connected_users = 0;
                                    }
                                }
                                this.server.updateStatus();
                                return;
                            }
                            if (!this.server_item.getProperty("serverType", "").toUpperCase().startsWith("PORTFORWARD")) break block91;
                            Socket sock2 = null;
                            try {
                                if (Common.dmz_mode) {
                                    Vector socket_queue = (Vector)Common.System2.get("crushftp.dmz.queue.sock");
                                    Properties mySock = new Properties();
                                    mySock.put("type", "GET:SOCKET");
                                    if (this.server_item.getProperty("pass_source_ip_as_proxy_v1", "false").equals("true")) {
                                        mySock.put("port", String.valueOf(this.server_item.getProperty("dest_ip")) + ":" + this.server_item.getProperty("dest_port") + ":" + ip + ":" + this.sock.getPort());
                                    } else {
                                        mySock.put("port", String.valueOf(this.server_item.getProperty("dest_ip")) + ":" + this.server_item.getProperty("dest_port"));
                                    }
                                    mySock.put("data", new Properties());
                                    mySock.put("id", String.valueOf(crushftp.handlers.Common.makeBoundary(10)) + new Date().getTime());
                                    mySock.put("sticky_token", "");
                                    mySock.put("created", String.valueOf(System.currentTimeMillis()));
                                    mySock.put("need_response", "true");
                                    long start = System.currentTimeMillis();
                                    int wait = 10;
                                    while (System.currentTimeMillis() - start < 30000L) {
                                        try {
                                            Socket sock = Common.grabDataSock(mySock);
                                            if (sock != null) {
                                                mySock.put("socket", sock);
                                                break;
                                            }
                                            Thread.sleep(wait);
                                            if (wait >= 100) continue;
                                            wait += 10;
                                        }
                                        catch (Exception sock) {
                                            // empty catch block
                                        }
                                    }
                                    if (mySock.get("socket") == null) {
                                        throw new Exception("failure: Waited 30 seconds for DMZ socket, giving up.");
                                    }
                                    sock2 = (Socket)mySock.remove("socket");
                                } else {
                                    sock2 = new Socket(this.server_item.getProperty("dest_ip"), Integer.parseInt(this.server_item.getProperty("dest_port")));
                                }
                                Log.log("SERVER", 1, String.valueOf(this.server_item.getProperty("serverType", "")) + ": ACCEPTED:" + ip + ":" + this.listen_port);
                                Common.streamCopier(this.sock, sock2, this.sock.getInputStream(), sock2.getOutputStream(), true, false, false);
                                Common.streamCopier(this.sock, sock2, sock2.getInputStream(), this.sock.getOutputStream(), false, false, false);
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, "" + e);
                            }
                            this.sock.close();
                            if (sock2 == null) break block92;
                            sock2.close();
                        }
                        if (!(this.server instanceof DMZServer1 || this.server instanceof DMZServer3 || this.server instanceof DMZServerCommon)) {
                            QuickConnect.remove_ip_count(ip);
                        }
                        GenericServer genericServer = this.server;
                        synchronized (genericServer) {
                            --this.server.connected_users;
                            if (this.server.connected_users < 0) {
                                this.server.connected_users = 0;
                            }
                        }
                        this.server.updateStatus();
                        return;
                    }
                    try {
                        if (!this.server_item.getProperty("serverType", "").toUpperCase().startsWith("SOCKS")) break block93;
                        if (ServerStatus.siIG("enterprise_level") <= 0) {
                            throw new Exception("SOCKS5 only valid for Enterprise licenses.");
                        }
                        SessionCrush serverSession = new SessionCrush(this.sock, i, ip, this.listen_port, this.listen_ip, this.listen_ip_port, this.server_item);
                        byte[] head = new byte[3];
                        InputStream in = this.sock.getInputStream();
                        OutputStream out2 = this.sock.getOutputStream();
                        StringBuffer proxyHeader = new StringBuffer();
                        int loops = 0;
                        while (in.available() < 3 && loops++ < 100) {
                            Thread.sleep(100L);
                        }
                        int read = in.read(head);
                        proxyHeader.append("(read:" + read + ":" + head[0] + "," + head[1] + "," + head[2] + ")");
                        String host = "";
                        int port = 0;
                        int type = -1;
                        byte socksmode = head[0];
                        if (socksmode == 4) {
                            port = head[2] * 256 + in.read();
                            host = String.valueOf(Math.abs(in.read())) + "." + Math.abs(in.read()) + "." + Math.abs(in.read()) + "." + Math.abs(in.read());
                            while (in.read() > 0) {
                            }
                            byte[] byArray = new byte[8];
                            byArray[1] = 90;
                            byArray[2] = 1;
                            byArray[3] = 1;
                            byArray[4] = 2;
                            byArray[5] = 2;
                            byArray[6] = 2;
                            byArray[7] = 2;
                            out2.write(byArray);
                            out2.flush();
                        } else if (socksmode == 5) {
                            byte[] b;
                            byte[] byArray = new byte[2];
                            byArray[0] = head[0];
                            out2.write(byArray);
                            out2.flush();
                            loops = 0;
                            while (in.available() < 3 && loops++ < 100) {
                                Thread.sleep(100L);
                            }
                            read = in.read(head);
                            proxyHeader.append("(read:" + read + ":" + head[0] + "," + head[1] + "," + head[2] + ")");
                            type = in.read();
                            proxyHeader.append("(type" + type + ")");
                            if (type == 0) {
                                type = in.read();
                                proxyHeader.append("(type" + type + ")");
                            }
                            if (type == 1) {
                                b = new byte[4];
                                loops = 0;
                                while (in.available() < b.length && loops++ < 100) {
                                    Thread.sleep(100L);
                                }
                                host = String.valueOf(in.read()) + "." + in.read() + "." + in.read() + "." + in.read();
                                port = in.read() * 256 + in.read();
                            } else if (type == 3) {
                                b = new byte[in.read()];
                                loops = 0;
                                while (in.available() < b.length && loops++ < 100) {
                                    Thread.sleep(100L);
                                }
                                in.read(b);
                                host = new String(b);
                                port = in.read() * 256 + in.read();
                            } else {
                                proxyHeader.append(":unknown:");
                                read = in.read(head);
                                proxyHeader.append("(read:" + read + ":" + head[0] + "," + head[1] + "," + head[2] + ")");
                                read = in.read(head);
                                proxyHeader.append("(read:" + read + ":" + head[0] + "," + head[1] + "," + head[2] + ")");
                                read = in.read(head);
                                proxyHeader.append("(read:" + read + ":" + head[0] + "," + head[1] + "," + head[2] + ")");
                            }
                            byte[] byArray2 = new byte[10];
                            byArray2[0] = 5;
                            byArray2[3] = 1;
                            out2.write(byArray2);
                            out2.flush();
                        }
                        Log.log("SERVER", 1, "SOCKS:header:" + proxyHeader.toString());
                        Log.log("SERVER", 1, "SOCKS:" + host + ":" + port);
                        Socket sock2 = new Socket(host, port);
                        Common.streamCopier(this.sock, sock2, sock2.getInputStream(), out2, true, true, true);
                        Common.streamCopier(this.sock, sock2, in, sock2.getOutputStream(), false, true, true);
                        serverSession.do_kill(null);
                    }
                    catch (Throwable throwable) {
                        if (!(this.server instanceof DMZServer1 || this.server instanceof DMZServer3 || this.server instanceof DMZServerCommon)) {
                            QuickConnect.remove_ip_count(ip);
                        }
                        GenericServer genericServer = this.server;
                        synchronized (genericServer) {
                            --this.server.connected_users;
                            if (this.server.connected_users < 0) {
                                this.server.connected_users = 0;
                            }
                        }
                        this.server.updateStatus();
                        throw throwable;
                    }
                    if (!(this.server instanceof DMZServer1 || this.server instanceof DMZServer3 || this.server instanceof DMZServerCommon)) {
                        QuickConnect.remove_ip_count(ip);
                    }
                    GenericServer genericServer = this.server;
                    synchronized (genericServer) {
                        --this.server.connected_users;
                        if (this.server.connected_users < 0) {
                            this.server.connected_users = 0;
                        }
                    }
                    this.server.updateStatus();
                    return;
                }
                ServerSessionFTP serverSessionFTP = new ServerSessionFTP(this.sock, i, ip, this.listen_port, this.listen_ip, this.listen_ip_port, this.server_item);
                if (this.server_item.getProperty("serverType", "").toUpperCase().equals("FTPS") && this.sni_keystore_used != null) {
                    serverSessionFTP.sni_keystore_used = this.sni_keystore_used;
                }
                serverSessionFTP.give_thread_pointer(Thread.currentThread());
                serverSessionFTP.run();
                if (!(this.server instanceof DMZServer1 || this.server instanceof DMZServer3 || this.server instanceof DMZServerCommon)) {
                    QuickConnect.remove_ip_count(ip);
                }
                GenericServer genericServer = this.server;
                synchronized (genericServer) {
                    --this.server.connected_users;
                    if (this.server.connected_users < 0) {
                        this.server.connected_users = 0;
                    }
                }
                this.server.updateStatus();
                return;
            }
            Thread.currentThread().setName("0.4-BANNED " + ip);
            if (this.server_item.getProperty("serverType", "").toUpperCase().equals("HTTP") || this.server_item.getProperty("serverType", "").toUpperCase().equals("HTTPS")) {
                out = this.sock.getOutputStream();
                out.write("HTTP/1.1 200 BANNED\r\n".getBytes());
                String msg = String.valueOf(ServerStatus.SG("banned_ip_message")) + "(" + ip + ").\r\n";
                out.write(("Content-Length: " + msg.length() + "\r\n").getBytes());
                out.write("\r\n".getBytes());
                out.write(msg.getBytes());
                out.close();
            } else if (this.server_item.getProperty("serverType", "").toUpperCase().equals("FTP")) {
                out = this.sock.getOutputStream();
                String msg = "421" + ServerStatus.SG("banned_ip_message") + "(" + ip + ").\r\n";
                out.write(msg.getBytes());
                out.close();
            }
            this.sock.close();
            try {
                ServerStatus.thisObj.append_log(String.valueOf(ServerStatus.thisObj.logDateFormat.format(new Date())) + "|---" + ServerStatus.SG("BANNED IP CONNECTION TERMINATED") + "---:" + ip, "DENIAL");
            }
            catch (Exception exception) {
                // empty catch block
            }
            ServerStatus.put_in("failed_logins", "" + (ServerStatus.IG("failed_logins") + 1));
            Thread.sleep(100L);
            if (!(this.server instanceof DMZServer1 || this.server instanceof DMZServer3 || this.server instanceof DMZServerCommon)) {
                QuickConnect.remove_ip_count(ip);
            }
            GenericServer genericServer = this.server;
            synchronized (genericServer) {
                --this.server.connected_users;
                if (this.server.connected_users < 0) {
                    this.server.connected_users = 0;
                }
            }
            this.server.updateStatus();
        }
        catch (Exception e) {
            try {
                this.sock.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
            Log.log("SERVER", 1, e);
        }
    }

    public static boolean validate_ip(String ip, Properties server_item) throws Exception {
        boolean ipAllowed = crushftp.handlers.Common.check_ip((Vector)ServerStatus.thisObj.server_info.get("ip_restrictions_temp"), ip);
        boolean notHammer = false;
        notHammer = server_item.getProperty("serverType", "").toUpperCase().indexOf("HTTP") >= 0 ? ServerStatus.thisObj.check_hammer_ip_http(ip) : ServerStatus.thisObj.check_hammer_ip(ip);
        Vector server_ips = (Vector)server_item.get("ip_restrictions");
        if ((ipAllowed &= crushftp.handlers.Common.check_ip((Vector)ServerStatus.server_settings.get("ip_restrictions"), ip)) && notHammer && (server_ips == null || crushftp.handlers.Common.check_ip(server_ips, ip))) {
            String addon;
            String string = addon = server_item.getProperty("serverType", "").toUpperCase().indexOf("HTTP") >= 0 ? "_http" : "";
            if (ServerStatus.IG("hammer_banning" + addon) > 0) {
                ServerStatus.siPUT("hammer_history" + addon, String.valueOf(ServerStatus.siSG("hammer_history" + addon)) + ip + "\r\n");
            }
            return true;
        }
        String[] never_ban = ServerStatus.SG("never_ban").split(",");
        int x = 0;
        while (x < never_ban.length) {
            if (!never_ban[x].trim().equals("") && Common.do_search(never_ban[x].trim(), ip, false, 0)) {
                return true;
            }
            ++x;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static int getUserLoginNum() {
        Object object = syncUserNumbers;
        synchronized (object) {
            int i = ServerStatus.siIG("user_login_num");
            if (i >= 0x7FFFFFF8) {
                i = 0;
            }
            ServerStatus.siPUT("user_login_num", String.valueOf(i + 1));
            return i + 1;
        }
    }
}

