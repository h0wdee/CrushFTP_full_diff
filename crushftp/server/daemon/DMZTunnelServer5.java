/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.Common;
import com.crushftp.client.Worker;
import crushftp.server.daemon.DMZTunnelClient;
import crushftp.server.daemon.DMZTunnelClient5;
import crushftp.server.daemon.SocketChunked;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class DMZTunnelServer5 {
    public String allowed_ips = "";
    String bind_ip = "0.0.0.0";
    int bind_port = 0;
    String dest_tunnel_ip = "127.0.0.1";
    int dest_tunnel_port = 0;
    ServerSocket ss = null;
    Properties reverse_tunnels = new Properties();
    StringBuffer die_now = new StringBuffer();
    static final String GUID = Common.makeBoundary(10);
    DMZTunnelClient5 dmz5 = null;

    public DMZTunnelServer5(String bind_ip, int bind_port, ServerSocket ss, String allowed_ips) {
        this.bind_ip = bind_ip;
        this.bind_port = bind_port;
        this.ss = ss;
        this.allowed_ips = allowed_ips;
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
        this.dmz5 = new DMZTunnelClient5(this.dest_tunnel_ip, this.dest_tunnel_port, "DMZ5", null, this.die_now, null);
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    Thread.currentThread().setName("DMZ5:DMZ5ServerListener");
                    String msg = "";
                    while (!DMZTunnelServer5.this.ss.isClosed()) {
                        Socket sock = null;
                        try {
                            sock = DMZTunnelServer5.this.ss.accept();
                            String incoming_ip1 = sock.getInetAddress().getHostAddress();
                            if (DMZTunnelServer5.this.allowed_ips.equals("") && !incoming_ip1.equals("127.0.0.1")) {
                                DMZTunnelServer5.this.allowed_ips = String.valueOf(incoming_ip1.substring(0, incoming_ip1.lastIndexOf(".") + 1)) + "*";
                            }
                            if (!DMZTunnelServer5.this.allowed_ips.equals("") && !Common.do_search(DMZTunnelServer5.this.allowed_ips, incoming_ip1, false, 0) && DMZTunnelServer5.this.allowed_ips.indexOf(incoming_ip1) < 0) {
                                msg = new Date() + "|DMZ5:IP " + incoming_ip1 + " was from an untrusted host and was denied DMZ server control. Allowed IPs: " + DMZTunnelServer5.this.allowed_ips;
                                System.out.println(msg);
                                Common.log("DMZ", 0, msg);
                                sock.close();
                                continue;
                            }
                            msg = new Date() + "|DMZ5:DMZ5Server read/write sock accepted:" + sock;
                            Common.log("DMZ", 0, msg);
                            sock.getInputStream().read();
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
                        DMZTunnelServer5.this.process(sock);
                        Common.log("DMZ", 2, new Date() + "|DMZ5:" + DMZTunnelServer5.this.dmz5.dmz5_info);
                    }
                }
                catch (Exception e) {
                    Common.log("DMZ", 1, e);
                }
                Common.log("DMZ", 0, "DMZ5:Server has stopped.");
                try {
                    DMZTunnelServer5.this.ss.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                DMZTunnelClient.reset_sockets();
            }
        }, "DMZ5Server");
    }

    public void close() {
        try {
            this.ss.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void process(final Socket sock) {
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        SocketChunked remote = new SocketChunked(sock);
                        Properties p = remote.readChunk();
                        int command = Integer.parseInt(p.getProperty("c"));
                        if (command == 1) {
                            DMZTunnelServer5.this.dmz5.route_socket_data_to_dmz(new Socket(DMZTunnelServer5.this.dest_tunnel_ip, DMZTunnelServer5.this.dest_tunnel_port), Long.parseLong(p.getProperty("id")), remote);
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

