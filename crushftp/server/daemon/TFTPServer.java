/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.GenericClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.server.QuickConnect;
import crushftp.server.daemon.GenericServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Properties;

public class TFTPServer
extends GenericServer {
    int connections = 0;
    int active = 0;
    public Object active_lock = new Object();
    public static final int BUFSIZE = 516;
    public static final short OP_RRQ = 1;
    public static final short OP_WRQ = 2;
    public static final short OP_DAT = 3;
    public static final short OP_ACK = 4;
    public static final short OP_ERR = 5;
    public static final short ERR_LOST = 0;
    public static final short ERR_FNF = 1;
    public static final short ERR_ACCESS = 2;

    public static void main(String[] args) {
        try {
            new TFTPServer(null).run();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public TFTPServer(Properties server_item) {
        super(server_item);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void updateStatus() {
        Object object = updateServerStatuses;
        synchronized (object) {
            this.updateStatusInit();
            if (this.socket_created) {
                this.server_item.put("display", "TFTP://" + this.listen_ip + ":" + this.listen_port + "/ is running. (" + this.active + " active, " + this.connections + " processed.)");
            } else {
                this.server_item.put("display", "TFTP://" + this.listen_ip + ":" + this.listen_port + "/ is stopped. (" + this.active + " active, " + this.connections + " processed.)");
            }
        }
    }

    @Override
    public void run() {
        this.init();
        try {
            this.getSocket();
            this.server_sock.close();
            DatagramSocket socket = new DatagramSocket(new InetSocketAddress(this.server_sock.getLocalPort()));
            Log.log("SERVER", 0, "TFTP:Listening at port " + socket.getLocalPort() + " for new requests to:");
            byte[] buf = new byte[516];
            while (this.socket_created && this.die_now.length() == 0) {
                this.updateStatus();
                try {
                    DatagramPacket receivePacket = new DatagramPacket(buf, buf.length);
                    socket.receive(receivePacket);
                    final InetSocketAddress clientAddress = new InetSocketAddress(receivePacket.getAddress(), receivePacket.getPort());
                    final Properties options = this.getSegments(buf);
                    Log.log("SERVER", 2, "TFTP:Options:" + options);
                    ++this.connections;
                    Worker.startWorker(new Runnable(){

                        /*
                         * WARNING - Removed try catching itself - possible behaviour change.
                         */
                        @Override
                        public void run() {
                            block18: {
                                Object object = TFTPServer.this.active_lock;
                                synchronized (object) {
                                    ++TFTPServer.this.active;
                                }
                                try {
                                    try {
                                        String ip = clientAddress.getAddress().getHostAddress();
                                        if (QuickConnect.validate_ip(ip, TFTPServer.this.server_item)) {
                                            DatagramSocket dsock = new DatagramSocket(0);
                                            dsock.connect(clientAddress);
                                            Log.log("SERVER", 0, "TFTP:" + (Integer.parseInt(String.valueOf(options.getProperty("type"))) == 1 ? "Download" : "Upload") + " request for " + options.getProperty("path") + " from " + ip + " using port " + clientAddress.getPort());
                                            TFTPServer.this.process_tftp_packet(dsock, options);
                                            dsock.close();
                                        }
                                    }
                                    catch (Exception e) {
                                        Log.log("SERVER", 1, e);
                                        Object object2 = TFTPServer.this.active_lock;
                                        synchronized (object2) {
                                            --TFTPServer.this.active;
                                            break block18;
                                        }
                                    }
                                }
                                catch (Throwable throwable) {
                                    Object object3 = TFTPServer.this.active_lock;
                                    synchronized (object3) {
                                        --TFTPServer.this.active;
                                    }
                                    throw throwable;
                                }
                                Object object4 = TFTPServer.this.active_lock;
                                synchronized (object4) {
                                    --TFTPServer.this.active;
                                }
                            }
                        }
                    });
                }
                catch (IOException e) {
                    Log.log("SERVER", 1, e);
                }
                this.updateStatus();
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
    }

    private void process_tftp_packet(DatagramSocket dsock, Properties options) throws Exception {
        block15: {
            String path = options.getProperty("path");
            String username = "";
            String password = "";
            if (path.indexOf(":") >= 0) {
                username = path.split(":")[0].trim();
                password = path.split(":")[1].trim();
                path = path.split(":")[2].trim();
            } else {
                username = this.server_item.getProperty("proxy_username", "anonymous");
                password = this.server_item.getProperty("proxy_password", "user@email.com");
            }
            VRL vrl = new VRL(String.valueOf(this.server_item.getProperty("proxy_protocol", "ftp")) + "://" + username + ":" + password + "@" + this.server_item.getProperty("proxy_host", "127.0.0.1") + ":" + this.server_item.getProperty("proxy_port", "21") + this.server_item.getProperty("proxy_path", "/"));
            GenericClient c = Common.getClient(vrl.toString(), "TFTP:", null);
            c.login(vrl.getUsername(), vrl.getPassword(), null);
            byte[] buf = new byte[512];
            long size = 0L;
            try {
                if (Integer.parseInt(String.valueOf(options.getProperty("type"))) == 1) {
                    try {
                        InputStream in = c.download(path, 0L, -1L, options.getProperty("mode").equals("octet"));
                        short num = 1;
                        int len = 512;
                        while (len == 512) {
                            len = in.read(buf);
                            if (len < 0) {
                                len = 0;
                            }
                            size += (long)len;
                            DatagramPacket sender = new DatagramPacket(ByteBuffer.allocate(516).putShort((short)3).putShort(num).put(buf, 0, len).array(), 4 + len);
                            short s = num;
                            num = (short)(s + 1);
                            if (this.ack_and_receive(dsock, sender, s, true) != null) continue;
                            Log.log("SERVER", 0, "TFTP:Error. Lost connection.");
                            this.send_error(dsock, (short)0, "Lost connection.");
                            break;
                        }
                        Log.log("SERVER", 0, "TFTP:Transfer complete:" + path + " " + size + " bytes.");
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, "TFTP:Error:" + e + ", Sending error packet.");
                        this.send_error(dsock, (short)1, "");
                    }
                    break block15;
                }
                if (Integer.parseInt(String.valueOf(options.getProperty("type"))) == 2) {
                    DatagramPacket dp;
                    short num = 0;
                    OutputStream out = c.upload(path, 0L, true, options.getProperty("mode").equals("octet"));
                    do {
                        dp = this.ack_and_receive(dsock, new DatagramPacket(ByteBuffer.allocate(516).putShort((short)4).putShort(num).array(), 4), (short)(num + 1), true);
                        num = (short)(num + 1);
                        if (dp == null) {
                            Log.log("SERVER", 0, "TFTP:Error. Lost connection.");
                            this.send_error(dsock, (short)0, "Lost connection.");
                            c.close();
                            c.delete(path);
                            Log.log("SERVER", 0, "TFTP:Deleting incomplete file.");
                            break block15;
                        }
                        try {
                            out.write(dp.getData(), 4, dp.getLength() - 4);
                            size += (long)(dp.getLength() - 4);
                        }
                        catch (IOException e) {
                            Log.log("SERVER", 0, "TFTP:Error:" + e);
                            this.send_error(dsock, (short)2, "Trouble writing data.");
                            break block15;
                        }
                    } while (dp.getLength() - 4 >= 512);
                    this.ack_and_receive(dsock, new DatagramPacket(ByteBuffer.allocate(516).putShort((short)4).putShort(num).array(), 4), (short)-1, false);
                    Log.log("SERVER", 0, "TFTP:Transfer complete:" + path + " " + size + " bytes.");
                    break block15;
                }
                Log.log("SERVER", 0, "TFTP:Ignoring unknown packet type.");
            }
            finally {
                c.close();
                c.logout();
            }
        }
    }

    private DatagramPacket ack_and_receive(DatagramSocket dsock, DatagramPacket sender, short expected_block, boolean do_receive) throws IOException {
        int retryCount = 1;
        while (retryCount++ <= 21) {
            block9: {
                block8: {
                    if (retryCount >= 20) {
                        Log.log("SERVER", 0, "ERROR:Timed out, closing connection.");
                        break;
                    }
                    dsock.send(sender);
                    dsock.setSoTimeout(retryCount * 1000);
                    if (do_receive) break block8;
                    dsock.setSoTimeout(0);
                    break;
                }
                DatagramPacket receiver = new DatagramPacket(new byte[516], 516);
                dsock.receive(receiver);
                if (this.parse_num(receiver) != expected_block) break block9;
                DatagramPacket datagramPacket = receiver;
                dsock.setSoTimeout(0);
                return datagramPacket;
            }
            try {
                try {
                    retryCount = 1;
                }
                catch (IOException e) {
                    Log.log("SERVER", 0, "TFTP:Error: " + e);
                    dsock.setSoTimeout(0);
                    continue;
                }
            }
            catch (Throwable throwable) {
                dsock.setSoTimeout(0);
                throw throwable;
            }
            dsock.setSoTimeout(0);
        }
        return null;
    }

    private short parse_num(DatagramPacket data) {
        ByteBuffer buffer = ByteBuffer.wrap(data.getData());
        if (buffer.getShort() == 5) {
            Log.log("SERVER", 0, "TFTP:Client is dead. Closing connection.");
            byte[] buf = buffer.array();
            int i = 4;
            while (i < buf.length) {
                if (buf[i] == 0) {
                    String msg = new String(buf, 4, i - 4);
                    if (buffer.getShort() == 1) {
                        Log.log("SERVER", 0, "TFTP:File not found: " + msg);
                        break;
                    }
                    if (buffer.getShort() == 2) {
                        Log.log("SERVER", 0, "TFTP:Access violation: " + msg);
                        break;
                    }
                    if (buffer.getShort() == 3) {
                        Log.log("SERVER", 0, "TFTP:Disk full: " + msg);
                        break;
                    }
                    if (buffer.getShort() == 4) {
                        Log.log("SERVER", 0, "TFTP:Illegal TFTP operation: " + msg);
                        break;
                    }
                    if (buffer.getShort() == 5) {
                        Log.log("SERVER", 0, "TFTP:Unknown TID: " + msg);
                        break;
                    }
                    if (buffer.getShort() == 6) {
                        Log.log("SERVER", 0, "TFTP:File already exists: " + msg);
                        break;
                    }
                    if (buffer.getShort() == 7) {
                        Log.log("SERVER", 0, "TFTP:No such user: " + msg);
                        break;
                    }
                    Log.log("SERVER", 0, "TFTP:Unknown: " + msg);
                    break;
                }
                ++i;
            }
            return -1;
        }
        return buffer.getShort();
    }

    private void send_error(DatagramSocket dsock, short errorCode, String s) {
        byte[] b = ByteBuffer.allocate(516).putShort((short)5).putShort(errorCode).put(s.getBytes()).put((byte)0).array();
        try {
            dsock.send(new DatagramPacket(b, b.length));
        }
        catch (IOException e) {
            Log.log("SERVER", 0, "TFTP:Problem sending error packet.");
            Log.log("SERVER", 2, e);
        }
    }

    private Properties getSegments(byte[] b) {
        int loc;
        Properties p = new Properties();
        p.put("type", String.valueOf(ByteBuffer.wrap(b).getShort()));
        int x = loc = 2;
        while (x < b.length) {
            if (b[x] == 0) {
                if (loc == x) break;
                if (p.size() == 1) {
                    p.put("path", new String(b, loc, x - loc));
                } else if (p.size() == 2) {
                    p.put("mode", new String(b, loc, x - loc));
                } else if (p.size() == 3) {
                    p.put("extra1", new String(b, loc, x - loc));
                } else if (p.size() == 4) {
                    p.put("size", new String(b, loc, x - loc));
                }
                loc = x + 1;
            }
            ++x;
        }
        if (p.size() == 3) {
            p.put("size", "512");
        }
        return p;
    }
}

