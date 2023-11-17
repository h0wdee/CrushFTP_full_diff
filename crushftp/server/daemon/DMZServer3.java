/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.Common;
import crushftp.handlers.Log;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.DMZServerCommon;
import crushftp.server.daemon.DMZTunnelClient;
import crushftp.server.daemon.ServerBeat;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class DMZServer3
extends DMZServerCommon {
    public DMZServer3(Properties server_item) {
        super(server_item);
    }

    @Override
    public void start_connection() throws Exception {
        StringBuffer die_now2 = this.die_now;
        while (die_now2.length() == 0) {
            try {
                if (this.read_sock != null) {
                    Common.sockLog(this.read_sock, "read_sock close at port restart");
                    DMZServer3.closeInOutSockRef(this.socks_in_out, this.read_sock);
                }
                if (this.write_sock != null) {
                    Common.sockLog(this.write_sock, "write_sock close at port restart");
                    DMZServer3.closeInOutSockRef(this.socks_in_out, this.write_sock);
                }
                if (this.dmz_tunnel_client_d != null) {
                    this.dmz_tunnel_client_d.close();
                }
                if (this.dmz_tunnel_client_s != null) {
                    this.dmz_tunnel_client_s.close();
                }
                String msg = "Creating tunnel flag sock to DMZ, ssl=" + System.getProperty("crushftp.dmz3.ssl", "true") + ", DMZv3=true, " + this.listen_ip + ":" + this.listen_port;
                System.out.println(new Date() + "|" + msg);
                Log.log("DMZ", 0, msg);
                if (System.getProperty("crushftp.dmz.tunnel_start", "false").equals("false")) {
                    if (System.getProperty("crushftp.dmz3.ssl", "true").equals("true")) {
                        this.makeTunnelSock(Common.getSSLSocket(this.listen_ip, this.listen_port, true));
                    } else {
                        this.makeTunnelSock(new Socket(this.listen_ip, this.listen_port));
                    }
                }
                if (ServerStatus.BG("dmzv3_two_sockets")) {
                    this.dmz_tunnel_client_d = new DMZTunnelClient(this.listen_ip, this.listen_port, "data2", null, this.die_now, this);
                    this.dmz_tunnel_client_d.go();
                    this.dmz_tunnel_client_s = new DMZTunnelClient(this.listen_ip, this.listen_port, "system", null, this.die_now, this);
                    this.dmz_tunnel_client_s.go();
                    break;
                }
                this.dmz_tunnel_client_s = new DMZTunnelClient(this.listen_ip, this.listen_port, "system", null, this.die_now, this);
                this.dmz_tunnel_client_s.go();
                this.dmz_tunnel_client_d = this.dmz_tunnel_client_s;
                break;
            }
            catch (Exception e) {
                this.busyMessage = "ERROR connecting to " + this.listen_ip + ":" + this.listen_port + " " + e;
                Thread.sleep(1000L);
            }
        }
        this.getNewWriteSock();
        this.getNewReadSock();
    }

    @Override
    public void getNewWriteSock() throws Exception {
        this.write_sock = new Socket("127.0.0.1", this.dmz_tunnel_client_s.getLocalPort());
        this.write_sock.setTcpNoDelay(true);
        Common.sockLog(this.write_sock, "write_sock create (in tunnel)");
        this.write_sock.setSoTimeout(10000);
        this.write_sock.getOutputStream().write("R".getBytes());
        this.write_sock.getOutputStream().flush();
        DMZServer3.createInOutSockRef(this.socks_in_out, this.write_sock);
    }

    @Override
    public void getNewReadSock() throws Exception {
        this.read_sock = new Socket("127.0.0.1", this.dmz_tunnel_client_s.getLocalPort());
        this.read_sock.setTcpNoDelay(true);
        Common.sockLog(this.read_sock, "read_sock create (in tunnel)");
        this.read_sock.setSoTimeout(10000);
        this.read_sock.getOutputStream().write("W".getBytes());
        this.read_sock.getOutputStream().flush();
        DMZServer3.createInOutSockRef(this.socks_in_out, this.read_sock);
    }

    @Override
    public Runnable getSocketReceiver() {
        Runnable r2 = new Runnable(){

            @Override
            public void run() {
                StringBuffer die_now2 = DMZServer3.this.die_now;
                while (die_now2.length() == 0) {
                    Properties in_out = (Properties)DMZServer3.this.socks_in_out.get(DMZServer3.this.read_sock);
                    try {
                        DataInputStream din = (DataInputStream)in_out.get("in");
                        DataOutputStream dout = (DataOutputStream)in_out.get("out");
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
                            if (len <= 0) continue;
                            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
                            Properties p = (Properties)ois.readObject();
                            ois.close();
                            Thread.currentThread().setName("DMZSender:responseQueue=" + DMZServer3.this.responseQueue.size() + " last write len=" + len + "(" + p.getProperty("type") + ") milliseconds=" + (end - start) + ", total millis=" + (System.currentTimeMillis() - start) + " last_write_info:" + DMZServer3.this.last_write_info);
                            DMZServer3.this.responseQueue.addElement(p);
                            ++DMZServer3.this.messages_received;
                        }
                        catch (SocketTimeoutException socketTimeoutException) {}
                    }
                    catch (Throwable e) {
                        Common.sockLog(DMZServer3.this.read_sock, "Failure with read_socket:" + e);
                        Log.log("DMZ", 0, e);
                        Log.log("DMZ", 0, "Restarting read sock due to socket failure:" + e);
                        try {
                            DMZServer3.closeInOutSockRef(DMZServer3.this.socks_in_out, DMZServer3.this.sock);
                            Thread.sleep(1000L);
                            DMZServer3.this.getNewReadSock();
                        }
                        catch (Exception e1) {
                            Log.log("DMZ", 0, e);
                            DMZServer3.this.socket_created = false;
                            die_now2.append(System.currentTimeMillis());
                        }
                    }
                }
            }
        };
        return r2;
    }

    @Override
    public Runnable getSocketConnector() {
        Runnable r1 = new Runnable(){

            @Override
            public void run() {
                Thread.currentThread().setPriority(1);
                StringBuffer die_now2 = DMZServer3.this.die_now;
                try {
                    try {
                        Vector pending_data_socks = new Vector();
                        while (die_now2.length() == 0) {
                            if (ServerStatus.BG("serverbeat_dmz_master") && !ServerBeat.current_master) {
                                Thread.sleep(1000L);
                                continue;
                            }
                            Socket tempSock = null;
                            while (pending_data_socks.size() >= ServerStatus.IG("dmz_socket_pool_size")) {
                                Thread.sleep(100L);
                            }
                            try {
                                tempSock = new Socket();
                                tempSock.setSoTimeout(1000);
                                int loops = 0;
                                while (loops++ < 30) {
                                    try {
                                        tempSock.connect(new InetSocketAddress("127.0.0.1", DMZServer3.this.dmz_tunnel_client_d.getLocalPort()));
                                        break;
                                    }
                                    catch (SocketTimeoutException e) {
                                        if (loops >= 29) {
                                            throw e;
                                        }
                                        Log.log("DMZ", 1, "Timeout #" + loops + " of 1 second when trying to establish connection to DMZ..." + e);
                                    }
                                }
                                Common.sockLog(tempSock, "tempSock create.  pending_data_socks size=" + pending_data_socks.size());
                                tempSock.setSoTimeout(0);
                                tempSock.setTcpNoDelay(true);
                                if (DMZServer3.this.checkLoggingSockneeded(tempSock)) continue;
                                DMZServer3.this.processDataSocket(tempSock, pending_data_socks);
                            }
                            catch (IOException e) {
                                Common.sockLog(tempSock, "tempSock IOException:" + e);
                                Log.log("DMZ", 1, e);
                                Thread.sleep(200L);
                                Common.sockLog(tempSock, "tempSock closing.  pending_data_socks size=" + pending_data_socks.size());
                                if (tempSock == null) continue;
                                tempSock.close();
                            }
                        }
                    }
                    catch (Exception e) {
                        Log.log("DMZ", 0, e);
                        DMZServer3.this.socket_created = false;
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

    @Override
    public String getDmzNameHostPort() {
        return String.valueOf(this.server_item.getProperty("server_item_name")) + ":127.0.0.1:" + this.dmz_tunnel_client_d.getLocalPort() + ":true";
    }

    public void makeTunnelSock(Socket sock_tmp) throws Exception {
        String msg = "Created tunnel flag sock to DMZ, sending 'T' flag:" + sock_tmp;
        System.out.println(new Date() + "|" + msg);
        Common.sockLog(sock_tmp, msg);
        Log.log("DMZ", 0, msg);
        sock_tmp.getOutputStream().write("T".getBytes());
        sock_tmp.getOutputStream().flush();
        Thread.sleep(2000L);
        sock_tmp.close();
        Thread.sleep(2000L);
        msg = "DMZ converted to v3 mode:" + sock_tmp;
        System.out.println(new Date() + "|" + msg);
        Common.sockLog(sock_tmp, msg);
        Log.log("DMZ", 0, msg);
    }
}

