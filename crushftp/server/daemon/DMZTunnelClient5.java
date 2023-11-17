/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.Common;
import com.crushftp.client.Worker;
import crushftp.server.daemon.DMZServerCommon;
import crushftp.server.daemon.SocketChunked;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class DMZTunnelClient5 {
    String dest_server_ip = "0.0.0.0";
    int dest_server_port = 0;
    int local_port = 0;
    int dmz_dest_port = 0;
    ServerSocket ss = null;
    boolean running = true;
    long last_write = 0L;
    Vector transfer_socket_pool = new Vector();
    public Properties dmz5_info = new Properties();
    StringBuffer die_now = new StringBuffer();
    DMZServerCommon dmz = null;

    public DMZTunnelClient5(String dest_server_ip, int dest_server_port, String name, byte[] tunnel_id2, StringBuffer die_now, DMZServerCommon dmz) {
        this.dest_server_ip = dest_server_ip;
        this.dest_server_port = dest_server_port;
        this.dmz5_info.put("in", "0");
        this.dmz5_info.put("out", "0");
        this.dmz = dmz;
    }

    public void go() throws Exception {
        this.ss = new ServerSocket(0, 1000, InetAddress.getByName("127.0.0.1"));
        this.local_port = this.ss.getLocalPort();
        final Properties status = new Properties();
        status.put("error", new Exception("Sockets could not connect..."));
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                block6: {
                    try {
                        try {
                            Common.log("DMZ", 0, "DMZ5 local tunnel port=" + DMZTunnelClient5.this.local_port);
                            status.put("status", "success");
                            while (DMZTunnelClient5.this.isRunning()) {
                                Socket sock = DMZTunnelClient5.this.ss.accept();
                                DMZTunnelClient5.this.route_socket_data_to_dmz(sock, Common.uidg(), null);
                                Common.log("DMZ", 2, new Date() + "|DMZ5:" + DMZTunnelClient5.this.dmz5_info);
                            }
                        }
                        catch (Exception e) {
                            Common.log("DMZ", 1, e);
                            status.put("error", e);
                            DMZTunnelClient5.this.setStopped();
                            break block6;
                        }
                    }
                    catch (Throwable throwable) {
                        DMZTunnelClient5.this.setStopped();
                        throw throwable;
                    }
                    DMZTunnelClient5.this.setStopped();
                }
                Common.log("DMZ", 0, "DMZ5:Stopped");
            }
        }, "DMZ5 Client");
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

    public int getLocalPort() {
        return this.local_port;
    }

    public void close() {
        this.setStopped();
        Exception e_stop = new Exception("DMZ5:Stopped by close().");
        Common.log("DMZ", 0, e_stop);
        try {
            this.ss.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.dmz != null) {
            this.dmz.triggerPortErrorAlert(e_stop);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public SocketChunked get_transfer_socket() throws Exception {
        while (true) {
            SocketChunked chunked_socket_tmp = null;
            Vector vector = this.transfer_socket_pool;
            synchronized (vector) {
                if (this.transfer_socket_pool.size() > 0) {
                    chunked_socket_tmp = (SocketChunked)this.transfer_socket_pool.remove(0);
                }
            }
            if (chunked_socket_tmp == null) break;
            if (chunked_socket_tmp.isClosed()) {
                chunked_socket_tmp.close();
                continue;
            }
            try {
                chunked_socket_tmp.writeChunk(null, 0, 0, 3);
                if (chunked_socket_tmp.readChunk(2000).getProperty("c").equals("4")) {
                    return chunked_socket_tmp;
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            chunked_socket_tmp.close();
        }
        Socket sock_tmp = null;
        sock_tmp = System.getProperty("crushftp.dmz3.ssl", "true").equals("true") ? Common.getSSLSocket(this.dest_server_ip, this.dest_server_port, true) : new Socket(this.dest_server_ip, this.dest_server_port);
        sock_tmp.getOutputStream().write("5".getBytes("UTF8"));
        sock_tmp.getOutputStream().flush();
        return new SocketChunked(sock_tmp);
    }

    public void release_transfer_socket(SocketChunked remote) {
        try {
            this.transfer_socket_pool.addElement(remote.clearId());
            this.dmz5_info.put("pool", String.valueOf(this.transfer_socket_pool.size()));
        }
        catch (IOException e) {
            remote.close();
        }
    }

    public void route_socket_data_to_dmz(final Socket local, final long id, final SocketChunked remote_tmp1) throws Exception {
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    boolean allow_pool;
                    SocketChunked remote_tmp2 = remote_tmp1;
                    boolean bl = allow_pool = remote_tmp1 == null;
                    if (remote_tmp2 == null) {
                        remote_tmp2 = DMZTunnelClient5.this.get_transfer_socket();
                        remote_tmp2.init(id);
                        remote_tmp2.writeChunk(null, 0, 0, 1);
                    }
                    DMZTunnelClient5.this.dmz5_info.put("pool", String.valueOf(DMZTunnelClient5.this.transfer_socket_pool.size()));
                    final SocketChunked remote = remote_tmp2;
                    final Properties socket_status = new Properties();
                    socket_status.put("reading", "true");
                    socket_status.put("writing", "true");
                    Worker.startWorker(new Runnable(){

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void run() {
                            block20: {
                                Properties properties = (this).DMZTunnelClient5.this.dmz5_info;
                                synchronized (properties) {
                                    (this).DMZTunnelClient5.this.dmz5_info.put("in", String.valueOf(Integer.parseInt((this).DMZTunnelClient5.this.dmz5_info.getProperty("in")) + 1));
                                }
                                long total_bytes = 0L;
                                long time = 0L;
                                long started = System.currentTimeMillis();
                                BufferedInputStream in = null;
                                if (Common.log("DMZ", 2, "")) {
                                    Common.log("DMZ", 2, "DMZ5:Opening in stream for socket:" + id);
                                }
                                boolean error = false;
                                try {
                                    try {
                                        byte[] b = new byte[32768];
                                        local.setSoTimeout(10000);
                                        in = new BufferedInputStream(local.getInputStream());
                                        while (DMZTunnelClient5.this.isRunning()) {
                                            Thread.currentThread().setName("DMZ5:route_socket_data_to_dmz:" + id + ":started=" + new Date(started) + " socklocal=" + local + " total_bytes=" + total_bytes + " last_write_time:" + time);
                                            int bytes_read = 0;
                                            try {
                                                bytes_read = in.read(b);
                                            }
                                            catch (SocketTimeoutException e) {
                                                remote.writeChunk(null, 0, 0, 3);
                                                continue;
                                            }
                                            catch (SocketException e) {
                                                if (("" + e).indexOf("Socket closed") >= 0) {
                                                    bytes_read = -1;
                                                }
                                                throw e;
                                            }
                                            if (bytes_read > 0) {
                                                if (Common.log("DMZ", 3, "")) {
                                                    Common.log("DMZ", 3, "DMZ5:received data:" + id + ":" + bytes_read);
                                                }
                                                remote.writeChunk(b, 0, bytes_read, 0);
                                                total_bytes += (long)bytes_read;
                                                continue;
                                            }
                                            if (bytes_read >= 0) {
                                                continue;
                                            }
                                            break;
                                        }
                                    }
                                    catch (Exception e) {
                                        error = true;
                                        Common.log("DMZ", 2, e);
                                        DMZTunnelClient5.this.doClose(in, null, socket_status, remote, error, id, local, false, allow_pool);
                                        break block20;
                                    }
                                }
                                catch (Throwable throwable) {
                                    DMZTunnelClient5.this.doClose(in, null, socket_status, remote, error, id, local, false, allow_pool);
                                    throw throwable;
                                }
                                DMZTunnelClient5.this.doClose(in, null, socket_status, remote, error, id, local, false, allow_pool);
                            }
                            Properties properties = (this).DMZTunnelClient5.this.dmz5_info;
                            synchronized (properties) {
                                (this).DMZTunnelClient5.this.dmz5_info.put("in", String.valueOf(Integer.parseInt((this).DMZTunnelClient5.this.dmz5_info.getProperty("in")) + -1));
                            }
                        }
                    }, "DMZ5:route_socket_data_to_dmz:" + id + ":started=" + new Date() + " sock=" + local);
                    Worker.startWorker(new Runnable(){

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void run() {
                            block15: {
                                Properties properties = (this).DMZTunnelClient5.this.dmz5_info;
                                synchronized (properties) {
                                    (this).DMZTunnelClient5.this.dmz5_info.put("out", String.valueOf(Integer.parseInt((this).DMZTunnelClient5.this.dmz5_info.getProperty("out")) + 1));
                                }
                                long total_bytes = 0L;
                                long time = 0L;
                                long started = System.currentTimeMillis();
                                BufferedOutputStream out = null;
                                if (Common.log("DMZ", 2, "")) {
                                    Common.log("DMZ", 2, "DMZ5:Opening out stream for socket:" + id);
                                }
                                boolean error = false;
                                boolean already_closed = false;
                                try {
                                    try {
                                        out = new BufferedOutputStream(local.getOutputStream());
                                        while (DMZTunnelClient5.this.isRunning()) {
                                            Thread.currentThread().setName("DMZ5:route_socket_data_from_dmz:" + id + ":started=" + new Date(started) + " socklocal=" + local + " total_bytes=" + total_bytes + " last_read_time:" + time);
                                            Properties p = remote.readChunk(300000);
                                            byte[] b = (byte[])p.get("b");
                                            int command = Integer.parseInt(p.getProperty("c"));
                                            long id2 = Long.parseLong(p.getProperty("id"));
                                            if (command == 4) continue;
                                            if (command == 2) {
                                                already_closed = true;
                                                break;
                                            }
                                            if (id2 != id) {
                                                throw new IOException("Chunked stream ID does not match our ID: id=" + id + "  id2=" + id2);
                                            }
                                            out.write(b);
                                            out.flush();
                                        }
                                    }
                                    catch (Exception e) {
                                        error = true;
                                        Common.log("DMZ", 2, e);
                                        DMZTunnelClient5.this.doClose(null, out, socket_status, remote, error, id, local, already_closed, allow_pool);
                                        break block15;
                                    }
                                }
                                catch (Throwable throwable) {
                                    DMZTunnelClient5.this.doClose(null, out, socket_status, remote, error, id, local, already_closed, allow_pool);
                                    throw throwable;
                                }
                                DMZTunnelClient5.this.doClose(null, out, socket_status, remote, error, id, local, already_closed, allow_pool);
                            }
                            Properties properties = (this).DMZTunnelClient5.this.dmz5_info;
                            synchronized (properties) {
                                (this).DMZTunnelClient5.this.dmz5_info.put("out", String.valueOf(Integer.parseInt((this).DMZTunnelClient5.this.dmz5_info.getProperty("out")) - 1));
                            }
                        }
                    }, "DMZ5:route_socket_data_from_dmz:" + id + ":started=" + new Date() + " sock=" + local);
                }
                catch (Exception e) {
                    Common.log("DMZ", 1, e);
                }
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void doClose(InputStream in, OutputStream out, Properties socket_status, SocketChunked remote, boolean error, long id, Socket local, boolean already_closed, boolean allow_pool) {
        if (Common.log("DMZ", 2, "")) {
            Common.log("DMZ", 2, "DMZ5:Closing " + (in != null ? "in" : "out") + " stream for socket:" + id);
        }
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
        catch (IOException iOException) {
            // empty catch block
        }
        Properties properties = socket_status;
        synchronized (properties) {
            if (in != null) {
                socket_status.put("writing", "false");
            }
            if (out != null) {
                socket_status.put("reading", "false");
            }
            try {
                if (!already_closed) {
                    remote.writeChunk(null, 0, 0, 2);
                }
            }
            catch (IOException iOException) {
                // empty catch block
            }
            if (in != null && socket_status.getProperty("reading").equals("false") || out != null && socket_status.getProperty("writing").equals("false")) {
                if (!error && allow_pool) {
                    this.release_transfer_socket(remote);
                } else {
                    remote.close();
                }
                try {
                    local.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
            }
        }
    }
}

