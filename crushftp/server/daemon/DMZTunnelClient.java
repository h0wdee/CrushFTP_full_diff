/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.Worker;
import crushftp.server.daemon.DMZServerCommon;
import crushftp.server.daemon.DMZTunnelServer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class DMZTunnelClient {
    String dest_server_ip = "0.0.0.0";
    int dest_server_port = 0;
    int local_port = 0;
    int dmz_dest_port = 0;
    ServerSocket ss = null;
    Socket tunnel_sock_read = null;
    Socket tunnel_sock_write = null;
    public static Properties global_socket_map = new Properties();
    public static Properties global_chunk_num_prop = new Properties();
    static final int DATA_SOCK = 0;
    static final int CREATE_SOCK = 1;
    static final int CLOSE_SOCK = 2;
    static final int ACK = 3;
    static final int NOOP = 4;
    static final int NEED_READ = 5;
    static final int GET_ACKS = 16;
    String name = "";
    Properties pending_acks = new Properties();
    Properties recently_acked = new Properties();
    public Vector ack_q = new Vector();
    DataOutputStream tunnel_out = null;
    DataInputStream tunnel_in = null;
    transient Object out_lock = new Object();
    byte[] tunnel_id = null;
    public String tunnel_id_str = "";
    StringBuffer die_now = null;
    boolean running = true;
    long last_write = 0L;
    boolean dead_read_sock = false;
    static boolean started_cleaner = false;
    String tunnel_guid = null;
    DMZServerCommon dmz = null;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public DMZTunnelClient(String dest_server_ip, int dest_server_port, String name, byte[] tunnel_id2, StringBuffer die_now, DMZServerCommon dmz) {
        this.dmz = dmz;
        this.dest_server_ip = dest_server_ip;
        this.dest_server_port = dest_server_port;
        this.die_now = die_now;
        this.tunnel_id = tunnel_id2;
        if (this.tunnel_id == null) {
            this.tunnel_id = new byte[]{(byte)Common.getRandomInt(), (byte)Common.getRandomInt(), (byte)Common.getRandomInt()};
        }
        int x = 0;
        while (x < this.tunnel_id.length) {
            this.tunnel_id_str = String.valueOf(this.tunnel_id_str) + Common.replace_str(String.valueOf(this.tunnel_id[x]) + "-", "-", "");
            ++x;
        }
        this.name = String.valueOf(name) + ":" + this.tunnel_id_str;
        this.start_random_failures();
        this.start_check_for_resend();
        Properties properties = global_socket_map;
        synchronized (properties) {
            if (!started_cleaner) {
                started_cleaner = true;
                try {
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    DMZTunnelClient.this.deleteOldFiles(new File_S(System.getProperty("crushftp.dmz_chunk_temp_storage", "./dmz_tmp/")), 3600000L);
                                }
                                catch (Exception e) {
                                    Common.log("DMZ", 1, e);
                                }
                                try {
                                    Thread.sleep(3600000L);
                                }
                                catch (Exception exception) {
                                }
                            }
                        }
                    }, "dmz_tmp cleaner");
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
    }

    public void deleteOldFiles(File_S f, long ms) {
        File_S[] l = (File_S[])f.listFiles();
        int x = 0;
        while (x < l.length) {
            if (l[x].isDirectory()) {
                this.deleteOldFiles(l[x], ms);
            }
            if (l[x].isDirectory() && System.currentTimeMillis() - l[x].lastModified() > ms * 48L) {
                l[x].delete();
            } else if (l[x].isFile() && System.currentTimeMillis() - l[x].lastModified() > ms) {
                l[x].delete();
            }
            try {
                Thread.sleep(1L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
            ++x;
        }
    }

    public void go() throws Exception {
        this.dead_read_sock = false;
        DMZTunnelClient.reset_sockets();
        this.ss = new ServerSocket(0, 1000, InetAddress.getByName("127.0.0.1"));
        this.local_port = this.ss.getLocalPort();
        final Properties status = new Properties();
        status.put("error", new Exception("Sockets could not connect..."));
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                block7: {
                    try {
                        try {
                            DMZTunnelClient.this.reconnect_sockets(true);
                            DMZTunnelClient.this.reconnect_sockets(false);
                            Worker.startWorker(new Runnable(){

                                @Override
                                public void run() {
                                    while (DMZTunnelClient.this.isRunning()) {
                                        try {
                                            DMZTunnelClient.this.route_tunnel_data_to_socket();
                                        }
                                        catch (Throwable e) {
                                            Common.log("DMZ", 0, e);
                                        }
                                        try {
                                            Thread.sleep(1000L);
                                            (this).DMZTunnelClient.this.dead_read_sock = false;
                                            DMZTunnelClient.this.reconnect_sockets(true);
                                        }
                                        catch (Exception exception) {
                                            // empty catch block
                                        }
                                    }
                                    Common.log("DMZ", 0, "DMZTUNNEL:Connection has stopped.:" + (this).DMZTunnelClient.this.name);
                                }
                            }, "TUNNEL:route_tunnel_data_to_socket:started=" + new Date() + ":" + DMZTunnelClient.this.name);
                            Common.log("DMZ", 0, "DMZ local tunnel port=" + DMZTunnelClient.this.local_port);
                            status.put("status", "success");
                            DMZTunnelClient.this.get_acks();
                            while (DMZTunnelClient.this.isRunning()) {
                                Socket sock = DMZTunnelClient.this.ss.accept();
                                long id = Common.uidg();
                                Properties p = new Properties();
                                p.put("sock", sock);
                                p.put("out", sock.getOutputStream());
                                p.put("close", "false");
                                p.put("q", new Vector());
                                global_socket_map.put(String.valueOf(id), p);
                                if (Common.log("DMZ", 2, "")) {
                                    Common.log("DMZ", 2, "DMZTUNNEL:Asking command channel for new socket:" + id + ":" + DMZTunnelClient.this.name);
                                }
                                DMZTunnelClient.this.write_sock(1, id, -1, -1, null);
                                DMZTunnelClient.this.route_socket_data_through_tunnel(sock, id);
                            }
                        }
                        catch (Exception e) {
                            Common.log("DMZ", 1, e);
                            status.put("error", e);
                            DMZTunnelClient.this.setStopped();
                            break block7;
                        }
                    }
                    catch (Throwable throwable) {
                        DMZTunnelClient.this.setStopped();
                        throw throwable;
                    }
                    DMZTunnelClient.this.setStopped();
                }
                Common.log("DMZ", 0, "DMZTUNNEL:Stopped:" + DMZTunnelClient.this.name);
            }
        }, "DMZ Tunnel Client");
        int loops = 0;
        while (!status.getProperty("status", "").equals("success") && loops++ < 30 && this.isRunning()) {
            Thread.sleep(100L);
        }
        if (!status.getProperty("status", "").equals("success")) {
            throw (Exception)status.get("error");
        }
    }

    public boolean isRunning() {
        return this.die_now.length() == 0 && this.running;
    }

    public void setStopped() {
        this.running = false;
    }

    public DataOutputStream get_out() {
        return this.tunnel_out;
    }

    public DataInputStream get_in() {
        return this.tunnel_in;
    }

    public void set_in(Socket sock) throws Exception {
        if (this.tunnel_sock_read != null) {
            Common.sockLog(this.tunnel_sock_read, "Closing prior tunnel read socket");
            this.tunnel_sock_read.close();
        }
        this.tunnel_sock_read = sock;
        this.tunnel_sock_read.setSoTimeout(5000);
        this.tunnel_in = new DataInputStream(sock.getInputStream());
        String msg = "TUNNEL:configured new READ socket " + sock + " processor thread:" + this.tunnel_id_str + ":" + this.name;
        Common.log("DMZ", 0, msg);
        Common.sockLog(sock, "Starting off with new tunnel read socket");
    }

    public void set_out(Socket sock) throws Exception {
        if (this.tunnel_sock_write != null) {
            Common.sockLog(this.tunnel_sock_write, "Closing prior tunnel write socket");
            this.tunnel_sock_write.close();
        }
        this.tunnel_sock_write = sock;
        this.tunnel_sock_write.setSoTimeout(5000);
        this.tunnel_out = new DataOutputStream(sock.getOutputStream());
        String msg = "TUNNEL:configured new WRITE socket " + sock + " processor thread:" + this.tunnel_id_str + ":" + this.name;
        Common.log("DMZ", 0, msg);
        Common.sockLog(sock, "Starting off with new tunnel write socket");
    }

    public int getLocalPort() {
        return this.local_port;
    }

    public void close() {
        this.setStopped();
        Common.log("DMZ", 0, new Exception("DMZTUNNEL:Stopped by close()."));
        try {
            this.ss.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.tunnel_sock_read.close();
            Common.sockLog(this.tunnel_sock_read, "Closing prior read socket");
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.tunnel_sock_write.close();
            Common.sockLog(this.tunnel_sock_write, "Closing prior write socket");
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    public void route_socket_data_through_tunnel(final Socket sock, final long id) {
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    long total_bytes = 0L;
                    long time = 0L;
                    long started = System.currentTimeMillis();
                    BufferedInputStream in = null;
                    if (Common.log("DMZ", 2, "")) {
                        Common.log("DMZ", 2, "DMZTUNNEL:Opening in stream for socket:" + id + ":" + DMZTunnelClient.this.name);
                    }
                    try {
                        try {
                            in = new BufferedInputStream(sock.getInputStream());
                            while (DMZTunnelClient.this.isRunning()) {
                                Thread.currentThread().setName("TUNNEL1:route_socket_data_through_tunnel:" + id + ":started=" + new Date(started) + " sock=" + sock + " total_bytes=" + total_bytes + " last_write_time:" + time + " name=" + DMZTunnelClient.this.name);
                                byte[] b = new byte[32768];
                                int bytes_read = ((InputStream)in).read(b);
                                if (bytes_read > 0) {
                                    if (Common.log("DMZ", 3, "")) {
                                        Common.log("DMZ", 3, "DMZTUNNEL:received data:" + id + ":" + bytes_read + ":" + DMZTunnelClient.this.name);
                                    }
                                    long start = System.currentTimeMillis();
                                    DMZTunnelClient.this.write_sock(0, id, -1, bytes_read, b);
                                    total_bytes += (long)bytes_read;
                                    time = System.currentTimeMillis() - start;
                                    if (time <= 5000L) continue;
                                    Common.log("DMZ", 0, "DMZTUNNEL:***********WARNING********* DMZ connection is delayed in writing to tunnel_out..." + time + "ms  name=" + DMZTunnelClient.this.name);
                                    continue;
                                }
                                if (bytes_read >= 0) continue;
                                break;
                            }
                        }
                        catch (Exception e) {
                            if (("" + e).indexOf("Socket closed") < 0) {
                                Common.log("DMZ", 2, e);
                            }
                            if (Common.log("DMZ", 2, "")) {
                                Common.log("DMZ", 2, "DMZTUNNEL:Closing in stream for socket:" + id + ":" + DMZTunnelClient.this.name);
                            }
                            try {
                                if (in != null) {
                                    ((InputStream)in).close();
                                }
                            }
                            catch (IOException iOException) {
                                // empty catch block
                            }
                            DMZTunnelClient.this.kill_off_sock(null, id, sock, DMZTunnelClient.this.pending_acks);
                            Thread.currentThread().setName("TUNNEL1:route_socket_data_through_tunnel:" + id + ":started=" + new Date(started) + " sock=" + sock + " total_bytes=" + total_bytes + " last_write_time:" + (System.currentTimeMillis() - time) + " name=" + DMZTunnelClient.this.name + " CLOSED");
                        }
                    }
                    finally {
                        if (Common.log("DMZ", 2, "")) {
                            Common.log("DMZ", 2, "DMZTUNNEL:Closing in stream for socket:" + id + ":" + DMZTunnelClient.this.name);
                        }
                        try {
                            if (in != null) {
                                ((InputStream)in).close();
                            }
                        }
                        catch (IOException iOException) {}
                        DMZTunnelClient.this.kill_off_sock(null, id, sock, DMZTunnelClient.this.pending_acks);
                        Thread.currentThread().setName("TUNNEL1:route_socket_data_through_tunnel:" + id + ":started=" + new Date(started) + " sock=" + sock + " total_bytes=" + total_bytes + " last_write_time:" + (System.currentTimeMillis() - time) + " name=" + DMZTunnelClient.this.name + " CLOSED");
                    }
                }
            }, "TUNNEL1:route_socket_data_through_tunnel:" + id + ":started=" + new Date() + " sock=" + sock + " name=" + this.name);
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    Properties p = (Properties)global_socket_map.get(String.valueOf(id));
                    OutputStream out = (OutputStream)p.get("out");
                    Vector q = (Vector)p.get("q");
                    int delay = 1;
                    long started = System.currentTimeMillis();
                    long total_bytes = 0L;
                    long time = 0L;
                    Common.log("DMZ", 2, "DMZTUNNEL:Opening out stream for socket:" + id + ":" + DMZTunnelClient.this.name);
                    while (DMZTunnelClient.this.isRunning()) {
                        Thread.currentThread().setName("TUNNEL2:route_socket_data_through_tunnel:" + id + ":started=" + new Date(started) + " sock=" + sock + " q_size=" + q.size() + " total_bytes=" + total_bytes + " last_write_time:" + (System.currentTimeMillis() - time) + " name=" + DMZTunnelClient.this.name);
                        if (q.size() > 0) {
                            try {
                                Object o = q.remove(0);
                                byte[] b2 = null;
                                if (o instanceof String) {
                                    String ref = o.toString();
                                    RandomAccessFile fin = new RandomAccessFile(String.valueOf(System.getProperty("crushftp.dmz_chunk_temp_storage", "./dmz_tmp/")) + id + "/" + ref.split(":")[0], "r");
                                    b2 = new byte[Integer.parseInt(ref.split(":")[1])];
                                    fin.readFully(b2);
                                    fin.close();
                                    new File(String.valueOf(System.getProperty("crushftp.dmz_chunk_temp_storage", "./dmz_tmp/")) + id + "/" + ref.split(":")[0]).delete();
                                } else {
                                    b2 = (byte[])o;
                                }
                                out.write(b2);
                                out.flush();
                                delay = 0;
                                total_bytes += (long)b2.length;
                                time = System.currentTimeMillis();
                            }
                            catch (IOException e) {
                                DMZTunnelClient.this.kill_off_sock(e, Integer.parseInt(p.getProperty("id")), (Socket)p.get("sock"), DMZTunnelClient.this.pending_acks);
                            }
                        } else {
                            if (p.getProperty("close", "").equals("true") || !p.containsKey("sock")) {
                                Thread.currentThread().setName("TUNNEL2:route_socket_data_through_tunnel:" + id + ":started=" + new Date(started) + " sock=" + sock + " total_bytes=" + total_bytes + " last_write_time:" + (System.currentTimeMillis() - time) + " name=" + DMZTunnelClient.this.name + " CLOSED");
                                Common.log("DMZ", 2, "DMZTUNNEL:Closing out stream for socket:" + id + ": p_exists=" + (p != null) + ":" + DMZTunnelClient.this.name);
                                global_socket_map.remove(String.valueOf(id));
                                try {
                                    ((Socket)p.remove("sock")).close();
                                }
                                catch (Exception exception) {
                                    // empty catch block
                                }
                                Common.recurseDelete(String.valueOf(System.getProperty("crushftp.dmz_chunk_temp_storage", "./dmz_tmp/")) + id + "/", false);
                                break;
                            }
                            try {
                                Thread.sleep(delay);
                            }
                            catch (InterruptedException interruptedException) {
                                // empty catch block
                            }
                        }
                        if (delay >= 1000) continue;
                        ++delay;
                    }
                }
            }, "TUNNEL2:route_socket_data_through_tunnel:" + id + ":started=" + new Date() + " sock=" + sock + " name=" + this.name);
        }
        catch (Exception e) {
            Common.log("DMZ", 1, e);
        }
    }

    public void route_tunnel_data_to_socket() throws Exception {
        int dmz_memory_queue = Integer.parseInt(System.getProperty("crushftp.dmz_memory_queue", "20"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[32768];
        while (this.isRunning()) {
            try {
                DataInputStream din = this.get_in();
                int command = din.readInt();
                long id = din.readLong();
                int chunk_num = din.readInt();
                boolean discard = false;
                if (chunk_num >= 0 && command != 3) {
                    discard = this.recently_acked.containsKey(String.valueOf(chunk_num));
                    this.ack_q.addElement(String.valueOf(chunk_num));
                    this.recently_acked.put(String.valueOf(chunk_num), String.valueOf(System.currentTimeMillis()));
                    if (discard) {
                        Common.log("DMZ", 1, "DMZTUNNEL:DISCARD:" + id + ":" + chunk_num + ":" + this.name);
                    }
                }
                if (command == 0) {
                    int bytes_to_read = din.readInt();
                    while (bytes_to_read > baos.size()) {
                        int bytes_read = din.read(b, 0, bytes_to_read - baos.size());
                        if (bytes_read < 0) {
                            throw new IOException("Tunnel reading socket closed!");
                        }
                        baos.write(b, 0, bytes_read);
                    }
                    Properties p = (Properties)global_socket_map.get(String.valueOf(id));
                    if (p != null && !discard) {
                        Vector q = (Vector)p.get("q");
                        if (q.size() < dmz_memory_queue) {
                            byte[] b2 = new byte[baos.size()];
                            System.arraycopy(baos.toByteArray(), 0, b2, 0, b2.length);
                            q.addElement(b2);
                        } else {
                            new File(String.valueOf(System.getProperty("crushftp.dmz_chunk_temp_storage", "./dmz_tmp/")) + id + "/").mkdirs();
                            long ref = Common.uidg();
                            RandomAccessFile fout = new RandomAccessFile(String.valueOf(System.getProperty("crushftp.dmz_chunk_temp_storage", "./dmz_tmp/")) + id + "/" + ref, "rw");
                            fout.setLength(0L);
                            fout.write(baos.toByteArray());
                            fout.close();
                            q.addElement(String.valueOf(ref) + ":" + baos.size());
                        }
                    }
                    baos.reset();
                    continue;
                }
                if (command == 1 && !discard) {
                    Properties p = new Properties();
                    try {
                        Socket sock = new Socket(this.dest_server_ip, this.dest_server_port);
                        sock.setTcpNoDelay(true);
                        p.put("sock", sock);
                        p.put("out", new BufferedOutputStream(sock.getOutputStream(), 0x100000));
                        p.put("id", String.valueOf(id));
                        p.put("close", "false");
                        p.put("q", new Vector());
                        global_socket_map.put(String.valueOf(id), p);
                        Common.log("DMZ", 2, "DMZTUNNEL:CREATE_SOCK:" + id + ":" + this.name);
                        this.route_socket_data_through_tunnel(sock, id);
                    }
                    catch (Exception e) {
                        this.kill_off_sock(e, id, (Socket)p.get("sock"), this.pending_acks);
                    }
                    continue;
                }
                if (command == 3) {
                    if (Common.log("DMZ", 2, "")) {
                        Common.log("DMZ", 2, "DMZTUNNEL:ACK:" + id + ":" + chunk_num + ":" + this.name);
                    }
                    this.pending_acks.remove(String.valueOf(chunk_num));
                    continue;
                }
                if (command == 4) {
                    if (!Common.log("DMZ", 2, "")) continue;
                    Common.log("DMZ", 2, "DMZTUNNEL:NOOP:" + id + ":" + this.name);
                    continue;
                }
                if (command == 5 && !discard) {
                    Common.log("DMZ", 1, "DMZTUNNEL:NEED_READ:" + id + ":" + this.name);
                    this.reconnect_sockets(false);
                    continue;
                }
                if (command == 16 && !discard) {
                    dmz_memory_queue = Integer.parseInt(System.getProperty("crushftp.dmz_memory_queue", "20"));
                    if (Common.log("DMZ", 2, "")) {
                        Common.log("DMZ", 2, "DMZTUNNEL:GET_ACKS:" + id + ":dmz_memory_queue=" + dmz_memory_queue + ":" + this.name);
                    }
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            DMZTunnelClient.this.respond_to_acks();
                        }
                    }, "TUNNEL:RESPOND_ACKS:started=" + new Date() + ":ack_q_size=" + this.ack_q.size() + ":" + this.name);
                    continue;
                }
                if (command != 2 || discard) continue;
                Properties p = (Properties)global_socket_map.get(String.valueOf(id));
                if (Common.log("DMZ", 2, "")) {
                    Common.log("DMZ", 2, "DMZTUNNEL:CLOSE_SOCK:" + id + ":" + chunk_num + ":" + this.name + " p_exists:" + (p != null));
                }
                if (p == null) continue;
                p.put("close", "true");
            }
            catch (Exception e) {
                Common.log("DMZ", 1, e);
                this.tunnel_sock_read.close();
                if (!Common.dmz_mode) break;
                this.dead_read_sock = true;
                Common.log("DMZ", 1, "DMZTUNNEL:dead_read_sock:" + this.name);
                break;
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Unable to fully structure code
     */
    public void write_sock(int command, long id, int chunk_num, int bytes_read, byte[] b) throws Exception {
        this_chunk_num = -1;
        var8_7 = DMZTunnelClient.global_chunk_num_prop;
        synchronized (var8_7) {
            if (chunk_num == -1) {
                n = Integer.parseInt(DMZTunnelClient.global_chunk_num_prop.getProperty(String.valueOf(this.dest_server_port) + ":" + this.dmz_dest_port, "1"));
                this_chunk_num = n++;
                if (n == 0x7FFFFFFE) {
                    n = 1;
                }
                DMZTunnelClient.global_chunk_num_prop.put(String.valueOf(this.dest_server_port) + ":" + this.dmz_dest_port, String.valueOf(n));
            } else {
                this_chunk_num = chunk_num;
            }
            // MONITOREXIT @DISABLED, blocks:[0, 3] lbl14 : MonitorExitStatement: MONITOREXIT : var8_7
            if (true) ** GOTO lbl69
        }
        do {
            var8_7 = this.out_lock;
            synchronized (var8_7) {
                try {
                    dout = this.get_out();
                    if (this.dead_read_sock) {
                        Common.log("DMZ", 1, "DMZTUNNEL:dead_read_sock, asking Internal for new read socket:" + this.name);
                        this.dead_read_sock = false;
                        dout.writeInt(5);
                        dout.writeLong(0L);
                        dout.writeInt(0);
                        dout.flush();
                    }
                    dout.writeInt(command);
                    dout.writeLong(id);
                    dout.writeInt(this_chunk_num);
                    if (b != null) {
                        dout.writeInt(bytes_read);
                        dout.write(b, 0, bytes_read);
                    }
                    dout.flush();
                    if (command != 3) {
                        p_data = new Properties();
                        p_data.put("command", String.valueOf(command));
                        p_data.put("id", String.valueOf(id));
                        p_data.put("bytes_read", String.valueOf(bytes_read));
                        if (b != null) {
                            p_data.put("b", b);
                        }
                        p_data.put("t", String.valueOf(System.currentTimeMillis()));
                        this.pending_acks.put(String.valueOf(this_chunk_num), p_data);
                        if (command != 0 && Common.log("DMZ", 2, "")) {
                            Common.log("DMZ", 2, "DMZTUNNEL:write_sock:chunk_num=" + this_chunk_num + ":id=" + id + ":bytes_read=" + bytes_read + ":command=" + command + ":t=" + p_data.getProperty("t") + ":" + this.name);
                        }
                    }
                    this.last_write = System.currentTimeMillis();
                    break;
                }
                catch (Exception e) {
                    Common.log("DMZ", 1, e);
                    this.tunnel_sock_write.close();
                    if (!Common.dmz_mode) {
                        this.reconnect_sockets(false);
                    }
                }
            }
            Thread.sleep(1000L);
lbl69:
            // 2 sources

        } while (this.isRunning());
    }

    public void reconnect_sockets(boolean read) throws Exception {
        Socket sock = null;
        sock = System.getProperty("crushftp.dmz3.ssl", "true").equals("true") ? Common.getSSLSocket(this.dest_server_ip, this.dest_server_port, true) : new Socket(this.dest_server_ip, this.dest_server_port);
        sock.setTcpNoDelay(true);
        Common.sockLog(sock, "Created tunnel master sock:" + (read ? "read" : "write"));
        sock.getOutputStream().write((read ? "r" : "w").getBytes());
        byte[] b1 = new byte[1];
        String tunnel_guid_tmp = "";
        int x = 0;
        while (x < DMZTunnelServer.GUID.length()) {
            sock.getInputStream().read(b1);
            tunnel_guid_tmp = String.valueOf(tunnel_guid_tmp) + new String(b1, "UTF8");
            ++x;
        }
        if (this.tunnel_guid == null) {
            this.tunnel_guid = tunnel_guid_tmp;
        }
        if (!this.tunnel_guid.equals(tunnel_guid_tmp)) {
            String msg = "DMZ instance was restarted...Restarting DMZ port.";
            System.out.println(msg);
            this.tunnel_guid = null;
            if (this.dmz != null) {
                this.dmz.die_now.append(msg);
                this.dmz.restart = true;
                this.dmz.error = new Exception(msg);
                this.dmz.current_thread.interrupt();
            }
            this.close();
            throw new Exception(msg);
        }
        sock.getOutputStream().write(this.tunnel_id);
        sock.getOutputStream().flush();
        if (read) {
            this.set_in(sock);
        } else {
            this.set_out(sock);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void respond_to_acks() {
        Object object = this.out_lock;
        synchronized (object) {
            while (true) {
                if (this.ack_q.size() <= 0) {
                    return;
                }
                int chunk_num = Integer.parseInt("" + this.ack_q.remove(0));
                try {
                    this.write_sock(3, 0L, chunk_num, -1, null);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        }
    }

    public void kill_off_sock(Exception e, long id, Socket sock, Properties pending_acks) {
        if (e != null) {
            e.printStackTrace();
            Common.log("DMZ", 1, e);
        }
        try {
            Properties p = (Properties)global_socket_map.remove(String.valueOf(id));
            if (p != null) {
                p.put("close", "true");
            }
            sock.close();
            Common.recurseDelete(String.valueOf(System.getProperty("crushftp.dmz_chunk_temp_storage", "./dmz_tmp/")) + id + "/", false);
            this.write_sock(2, id, -1, -1, null);
        }
        catch (Exception e1) {
            Common.log("DMZ", 1, e1);
        }
    }

    public static void reset_sockets() {
        Enumeration<Object> keys = global_socket_map.keys();
        while (keys.hasMoreElements()) {
            String key = "" + keys.nextElement();
            Properties p = (Properties)global_socket_map.remove(key);
            try {
                if (p == null) continue;
                p.put("close", "true");
                ((Socket)p.remove("sock")).close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public void get_acks() throws Exception {
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                long time = 0L;
                while (DMZTunnelClient.this.isRunning()) {
                    try {
                        Thread.currentThread().setName("TUNNEL:GET_ACKS:" + DMZTunnelClient.this.tunnel_sock_write + " last_write_time:" + time + "ms ack_q.size():" + DMZTunnelClient.this.ack_q.size() + ":" + new Date() + "name=" + DMZTunnelClient.this.name);
                        DMZTunnelClient.this.respond_to_acks();
                        Thread.sleep(1000L);
                        long start = System.currentTimeMillis();
                        DMZTunnelClient.this.write_sock(16, 0L, -1, -1, null);
                        time = System.currentTimeMillis() - start;
                    }
                    catch (Exception e) {
                        Common.log("DMZ", 1, e);
                    }
                }
            }
        }, "TUNNEL:GET_ACKS:" + this.tunnel_sock_write + " name=" + this.name);
    }

    public void start_check_for_resend() {
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    long time = System.currentTimeMillis() + 2000L;
                    while (DMZTunnelClient.this.isRunning()) {
                        try {
                            String chunk_num;
                            Thread.currentThread().setName("TUNNEL:ACK RETRANSMIT/CLEAN pending_acks:" + DMZTunnelClient.this.pending_acks.size() + " recently_acked:" + DMZTunnelClient.this.recently_acked.size() + " time:" + (System.currentTimeMillis() - time - 2000L) + "ms name:" + DMZTunnelClient.this.name);
                            time = System.currentTimeMillis();
                            Enumeration<Object> keys = DMZTunnelClient.this.pending_acks.keys();
                            while (keys.hasMoreElements()) {
                                chunk_num = "" + keys.nextElement();
                                Properties p = (Properties)DMZTunnelClient.this.pending_acks.get(chunk_num);
                                if (System.currentTimeMillis() - Long.parseLong(p.getProperty("t")) <= 5000L) continue;
                                p.put("t", String.valueOf(System.currentTimeMillis()));
                                try {
                                    Common.log("DMZ", 0, "DMZTUNNEL:RESEND ON OLD DATA!!!:chunk_num=" + chunk_num + ":id=" + p.getProperty("id") + ":bytes_read=" + p.getProperty("bytes_read") + ":command=" + p.getProperty("command") + ":t=" + p.getProperty("t") + ":" + DMZTunnelClient.this.name);
                                    DMZTunnelClient.this.write_sock(Integer.parseInt(p.getProperty("command")), Long.parseLong(p.getProperty("id")), Integer.parseInt(chunk_num), Integer.parseInt(p.getProperty("bytes_read")), (byte[])p.get("b"));
                                }
                                catch (Exception e) {
                                    Common.log("DMZ", 1, e);
                                }
                                p.put("t", String.valueOf(System.currentTimeMillis()));
                            }
                            keys = DMZTunnelClient.this.recently_acked.keys();
                            while (keys.hasMoreElements()) {
                                chunk_num = "" + keys.nextElement();
                                if (System.currentTimeMillis() - Long.parseLong(DMZTunnelClient.this.recently_acked.getProperty(chunk_num)) <= 15000L) continue;
                                DMZTunnelClient.this.recently_acked.remove(chunk_num);
                            }
                        }
                        catch (Exception e) {
                            Common.log("DMZ", 1, e);
                        }
                        try {
                            Thread.sleep(2000L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                    }
                }
            }, "TUNNEL:ACK RETRANSMIT/CLEAN name:" + this.name);
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    long time = System.currentTimeMillis() + 1000L;
                    while (DMZTunnelClient.this.isRunning()) {
                        try {
                            Thread.currentThread().setName("TUNNEL:NOOP KEEP ALIVE pending_acks:" + DMZTunnelClient.this.pending_acks.size() + " recently_acked:" + DMZTunnelClient.this.recently_acked.size() + " time:" + (System.currentTimeMillis() - time - 1000L) + "ms name:" + DMZTunnelClient.this.name);
                            time = System.currentTimeMillis();
                            if (System.currentTimeMillis() - DMZTunnelClient.this.last_write > 1000L) {
                                DMZTunnelClient.this.write_sock(4, 0L, -1, -1, null);
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (InterruptedException interruptedException) {
                            // empty catch block
                        }
                    }
                }
            }, "TUNNEL:NOOP KEEP ALIVE name:" + this.name);
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public boolean random_discard() {
        return Common.makeBoundary(5).indexOf("a") >= 0;
    }

    public void start_random_failures() {
    }
}

