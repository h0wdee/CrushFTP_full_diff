/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.Common;
import com.crushftp.client.Worker;
import crushftp.server.daemon.DMZTunnelClient;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class DMZTunnelServer {
    public String allowed_ips = "";
    String bind_ip = "0.0.0.0";
    int bind_port = 0;
    String dest_tunnel_ip = "127.0.0.1";
    int dest_tunnel_port = 0;
    ServerSocket ss = null;
    Properties reverse_tunnels = new Properties();
    StringBuffer die_now = new StringBuffer();
    static final String GUID = Common.makeBoundary(10);

    public DMZTunnelServer(String bind_ip, int bind_port, ServerSocket ss) {
        this.bind_ip = bind_ip;
        this.bind_port = bind_port;
        this.ss = ss;
        this.dest_tunnel_port = bind_port + 1;
    }

    public void startup() throws Exception {
        if (this.ss == null) {
            this.ss = System.getProperty("crushftp.dmz3.ssl", "true").equals("true") ? Common.getSSLServerSocket(this.bind_port, this.bind_ip, true, "builtin", "crushftp", "crushftp") : new ServerSocket(this.bind_port, 1000, InetAddress.getByName(this.bind_ip));
        }
        this.resetOldDataThreads();
        if (this.ss != null) {
            this.ss.setSoTimeout(1000);
        }
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    Thread.currentThread().setName("DMZTUNNEL:DMZTunnelServerListener");
                    String msg = "";
                    while (!DMZTunnelServer.this.ss.isClosed()) {
                        byte[] b = new byte[1];
                        byte[] tunnel_id = new byte[3];
                        Socket sock = null;
                        try {
                            sock = DMZTunnelServer.this.ss.accept();
                            String incoming_ip1 = sock.getInetAddress().getHostAddress();
                            if (DMZTunnelServer.this.allowed_ips.equals("") && !incoming_ip1.equals("127.0.0.1")) {
                                DMZTunnelServer.this.allowed_ips = String.valueOf(incoming_ip1.substring(0, incoming_ip1.lastIndexOf(".") + 1)) + "*";
                            }
                            if (!DMZTunnelServer.this.allowed_ips.equals("") && !Common.do_search(DMZTunnelServer.this.allowed_ips, incoming_ip1, false, 0) && DMZTunnelServer.this.allowed_ips.indexOf(incoming_ip1) < 0) {
                                msg = new Date() + "|DMZTUNNEL:IP " + incoming_ip1 + " was from an untrusted host and was denied DMZ server control. Allowed IPs: " + DMZTunnelServer.this.allowed_ips;
                                System.out.println(msg);
                                Common.log("DMZ", 0, msg);
                                sock.close();
                                continue;
                            }
                            msg = new Date() + "|DMZTUNNEL:DMZTunnelServer read/write sock accepted:" + sock;
                            Common.log("DMZ", 0, msg);
                            sock.getInputStream().read(b);
                        }
                        catch (SocketTimeoutException e) {
                            continue;
                        }
                        catch (Exception e) {
                            if (sock != null) {
                                sock.close();
                            }
                            Common.log("DMZ", 0, e);
                            continue;
                        }
                        try {
                            String read_write = new String(b);
                            if (read_write.equals("T")) {
                                DMZTunnelServer.this.die_now.append(System.currentTimeMillis());
                                DMZTunnelServer.this.die_now = new StringBuffer();
                                msg = new Date() + "|DMZTUNNEL:Got tunnel start flag, DMZ port restarted, closing all old tunnels and starting fresh.";
                                System.out.println(msg);
                                Common.log("DMZ", 0, msg);
                                Enumeration<Object> keys = DMZTunnelServer.this.reverse_tunnels.keys();
                                while (keys.hasMoreElements()) {
                                    String key = "" + keys.nextElement();
                                    msg = new Date() + "|DMZTUNNEL:closing tunnel:" + key;
                                    System.out.println(msg);
                                    Common.log("DMZ", 0, msg);
                                    DMZTunnelClient dmzt = (DMZTunnelClient)DMZTunnelServer.this.reverse_tunnels.remove(key);
                                    dmzt.setStopped();
                                    dmzt.close();
                                }
                                sock.close();
                                continue;
                            }
                            sock.getOutputStream().write(GUID.getBytes("UTF8"));
                            sock.getOutputStream().flush();
                            String tunnel_id_str = "";
                            byte[] b2 = new byte[1];
                            int x = 0;
                            while (x < 3) {
                                sock.getInputStream().read(b2);
                                tunnel_id[x] = b2[0];
                                tunnel_id_str = String.valueOf(tunnel_id_str) + Common.replace_str(String.valueOf(tunnel_id[x]) + "-", "-", "");
                                ++x;
                            }
                            msg = new Date() + "|DMZTUNNEL:Got socket, id=" + tunnel_id_str + ":" + sock;
                            System.out.println(msg);
                            Common.log("DMZ", 0, msg);
                            if (!read_write.equals("r") && !read_write.equals("w")) {
                                sock.close();
                                continue;
                            }
                            if (!DMZTunnelServer.this.reverse_tunnels.containsKey(tunnel_id_str)) {
                                DMZTunnelClient dmz_tunnel_reverse = new DMZTunnelClient(DMZTunnelServer.this.dest_tunnel_ip, DMZTunnelServer.this.dest_tunnel_port, "dmz", tunnel_id, DMZTunnelServer.this.die_now, null);
                                DMZTunnelServer.this.reverse_tunnels.put(tunnel_id_str, dmz_tunnel_reverse);
                                msg = new Date() + "|DMZTUNNEL:starting processor thread:" + tunnel_id_str;
                                System.out.println(msg);
                                Common.log("DMZ", 0, msg);
                                DMZTunnelServer.this.start_reverse_processor(dmz_tunnel_reverse);
                            }
                            DMZTunnelClient dmz_tunnel_reverse = (DMZTunnelClient)DMZTunnelServer.this.reverse_tunnels.get(tunnel_id_str);
                            if (read_write.equals("w")) {
                                dmz_tunnel_reverse.set_in(sock);
                                continue;
                            }
                            if (!read_write.equals("r")) continue;
                            dmz_tunnel_reverse.set_out(sock);
                        }
                        catch (SocketTimeoutException read_write) {
                        }
                        catch (Exception e) {
                            if (sock != null) {
                                sock.close();
                            }
                            Common.log("DMZ", 0, e);
                            DMZTunnelClient.reset_sockets();
                        }
                    }
                }
                catch (Exception e) {
                    Common.log("DMZ", 1, e);
                }
                Common.log("DMZ", 0, "DMZTUNNEL:Server has stopped.");
                try {
                    DMZTunnelServer.this.ss.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                DMZTunnelClient.reset_sockets();
            }
        }, "DMZTunnelServer");
    }

    public void start_reverse_processor(final DMZTunnelClient dmz_tunnel_reverse) {
        try {
            Worker.startWorker(new Runnable(){

                /*
                 * Unable to fully structure code
                 */
                @Override
                public void run() {
                    try {
                        try {
                            block11: {
                                while (dmz_tunnel_reverse.get_in() == null || dmz_tunnel_reverse.get_out() == null && dmz_tunnel_reverse.isRunning()) {
                                    Thread.sleep(1000L);
                                }
                                break block11;
                                while (true) {
                                    try {
                                        dmz_tunnel_reverse.route_tunnel_data_to_socket();
                                    }
                                    catch (Exception e) {
                                        Common.log("DMZ", 1, e);
                                    }
                                    Thread.sleep(1000L);
                                    break;
                                }
                            }
                            if (!DMZTunnelServer.this.ss.isClosed()) {
                                if (dmz_tunnel_reverse.isRunning()) ** continue;
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Common.log("DMZ", 1, e);
                            dmz_tunnel_reverse.setStopped();
                        }
                    }
                    finally {
                        dmz_tunnel_reverse.setStopped();
                    }
                }
            }, "DMZServer read/write reverse socket processor");
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void close() {
        try {
            this.ss.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void resetOldDataThreads() {
        try {
            Worker.startWorker(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 * Enabled aggressive block sorting
                 * Enabled unnecessary exception pruning
                 * Enabled aggressive exception aggregation
                 */
                @Override
                public void run() {
                    try {
                        Thread.sleep(2000L);
                        Vector data_sock_available = (Vector)Common.System2.get("crushftp.dmz.data_sock_available");
                        if (data_sock_available == null) return;
                        Vector vector = data_sock_available;
                        synchronized (vector) {
                            while (true) {
                                if (data_sock_available.size() <= 0) {
                                    return;
                                }
                                try {
                                    Properties p = (Properties)data_sock_available.remove(0);
                                    ((Socket)p.remove("sock")).close();
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                            }
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            });
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

