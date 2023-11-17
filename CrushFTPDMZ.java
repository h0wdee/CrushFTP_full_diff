/*
 * Decompiled with CFR 0.152.
 */
import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.Worker;
import crushftp.handlers.Log;
import crushftp.handlers.SharedSession;
import crushftp.server.AdminControls;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.DMZServerCommon;
import crushftp.server.daemon.DMZTunnelServer;
import crushftp.server.daemon.DMZTunnelServer5;
import crushftp.server.daemon.DMZTunnelServerSSH;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.SSLServerSocket;

public class CrushFTPDMZ {
    public static Vector queue = new Vector();
    public static Properties dmzResponses = new Properties();
    boolean started = false;
    boolean starting = false;
    Vector read_socks = new Vector();
    Vector write_socks = new Vector();
    Properties socks_in_out = new Properties();
    crushftp.handlers.Common common_code = new crushftp.handlers.Common();
    public static Vector data_sock_available = new Vector();
    public static transient Object data_sock_available_lock = new Object();
    boolean servers_stopped = false;
    int servers_stopped_count = 0;
    String command_ip_main = "127.0.0.1";
    int command_port_main = 0;
    DMZTunnelServer dmzt3 = null;
    DMZTunnelServerSSH dmzt4 = null;
    DMZTunnelServer5 dmzt5 = null;
    static transient Object tunnel_start_lock = new Object();
    String allowed_ips = "";

    public CrushFTPDMZ(String[] args) {
        Common.dmz_mode = System.getProperty("crushftp.dmz", "true").equals("true");
        Common.System2.put("crushftp.dmz.queue", queue);
        Common.System2.put("crushftp.dmz.data_sock_available", data_sock_available);
        Common.System2.put("crushftp.dmz.data_sock_available_lock", data_sock_available_lock);
        System.getProperties().put("crushftp.worker.v9", System.getProperty("crushftp.worker.v9", "true"));
        String[] port_and_ips = args[1].split(",");
        int[] listen_ports = new int[port_and_ips.length];
        String[] listen_ips = new String[port_and_ips.length];
        int x = 0;
        while (x < port_and_ips.length) {
            if (port_and_ips[x].indexOf(":") >= 0) {
                listen_ports[x] = Integer.parseInt(port_and_ips[x].split(":")[1]);
                listen_ips[x] = port_and_ips[x].split(":")[0];
            } else {
                listen_ports[x] = Integer.parseInt(port_and_ips[x]);
                listen_ips[x] = "0.0.0.0";
            }
            ++x;
        }
        String[] args2 = args;
        String allowed_ips_tmp = "";
        if (args2.length >= 3) {
            allowed_ips_tmp = args2[2].trim();
        }
        this.allowed_ips = allowed_ips_tmp;
        try {
            this.closeExpiredDataSocks();
            int x2 = 0;
            while (x2 < listen_ports.length) {
                final int command_port2 = listen_ports[x2];
                final String command_ip2 = listen_ips[x2];
                if (this.command_port_main == 0) {
                    this.command_ip_main = command_ip2;
                    this.command_port_main = command_port2;
                }
                final int loop_num = x2++;
                Worker.startWorker(new Runnable(){

                    /*
                     * Unable to fully structure code
                     */
                    @Override
                    public void run() {
                        Thread.currentThread().setName("DMZCommandSocketReceiver:" + loop_num);
                        try {
                            command_ip = command_ip2;
                            command_port = command_port2;
                            System.out.println(new Date() + "|" + System.getProperty("java.home") + "/bin/java\r\n" + System.getProperty("java.version") + ", " + System.getProperty("sun.arch.data.model") + " bit\r\n" + System.getProperties().getProperty("os.name"));
                            System.out.println(new Date() + "|Waiting for DMZ connection from internal server on port " + command_port + ".");
                            ss_data_wrapper = new Properties();
                            ss_data_wrapper.put("ss_data", CrushFTPDMZ.this.getServerSocket(command_ip, command_port));
                            sock = null;
                            while (true) lbl-1000:
                            // 7 sources

                            {
                                try {
                                    while (true) {
                                        ss_data = (ServerSocket)ss_data_wrapper.get("ss_data");
                                        if (System.getProperty("crushftp.dmz.tunnel", "false").equals("true") && !CrushFTPDMZ.this.allowed_ips.equals("")) {
                                            CrushFTPDMZ.this.allowed_ips = "127.0.0.1";
                                        }
                                        if (System.getProperty("crushftp.dmz.tunnel", "false").equals("false")) {
                                            ss_data.setSoTimeout(DMZServerCommon.MAX_DMZ_SOCKET_IDLE_TIME + 1000);
                                        }
                                        sock = ss_data.accept();
                                        CrushFTPDMZ.this.servers_stopped_count = 0;
                                        if (CrushFTPDMZ.this.servers_stopped) {
                                            if (ServerStatus.BG("stop_dmz_ports_internal_down")) {
                                                CrushFTPDMZ.this.servers_stopped = false;
                                                ServerStatus.thisObj.start_all_servers();
                                            }
                                        }
                                        Common.sockLog(sock, "DMZ Incoming");
                                        incoming_ip = sock.getInetAddress().getHostAddress();
                                        if (!CrushFTPDMZ.this.allowed_ips.equals("") && !Common.do_search(CrushFTPDMZ.this.allowed_ips, incoming_ip, false, 0) && CrushFTPDMZ.this.allowed_ips.indexOf(incoming_ip) < 0) {
                                            System.out.println(new Date() + "|IP " + sock.getInetAddress().getHostAddress() + " was from an untrusted host and was denied DMZ server control. Allowed IPs: " + CrushFTPDMZ.this.allowed_ips);
                                            sock.close();
                                            continue;
                                        }
                                        if (CrushFTPDMZ.this.allowed_ips.equals("")) {
                                            CrushFTPDMZ.this.allowed_ips = String.valueOf(incoming_ip.substring(0, incoming_ip.lastIndexOf(".") + 1)) + "*";
                                            if (System.getProperty("crushftp.dmz.tunnel", "false").equals("true") && !CrushFTPDMZ.this.allowed_ips.equals("")) {
                                                CrushFTPDMZ.this.allowed_ips = "127.0.0.1";
                                            }
                                            System.out.println(new Date() + "|IP " + sock.getInetAddress().getHostAddress() + " attempting connection...allowed_ips=" + CrushFTPDMZ.this.allowed_ips);
                                        }
                                        allowed_ips2 = CrushFTPDMZ.this.allowed_ips;
                                        sock_thread = sock;
                                        Worker.startWorker(new Runnable(){

                                            /*
                                             * WARNING - Removed try catching itself - possible behaviour change.
                                             * Enabled force condition propagation
                                             * Lifted jumps to return sites
                                             */
                                            @Override
                                            public void run() {
                                                Socket sock2 = sock_thread;
                                                try {
                                                    try {
                                                        if (System.getProperty("crushftp.dmz.tunnel", "false").equals("false")) {
                                                            sock2.setSoTimeout(DMZServerCommon.MAX_DMZ_SOCKET_IDLE_TIME);
                                                        }
                                                        if (!(this).CrushFTPDMZ.this.started) {
                                                            System.out.println(new Date() + "|Reading socket type..." + sock2);
                                                        }
                                                        int i = sock2.getInputStream().read();
                                                        if (!(this).CrushFTPDMZ.this.started) {
                                                            System.out.println(new Date() + "|Read socket type..." + (char)i + ":" + sock2);
                                                        }
                                                        if (i == 82) {
                                                            Log.log("SERVER", 1, "DMZ READ Socket Connected:" + sock2);
                                                            System.out.println(new Date() + "|DMZ READ Socket Connected.");
                                                            Common.sockLog(sock2, "DMZ read_sock");
                                                            (this).CrushFTPDMZ.this.read_socks.addElement(sock2);
                                                            DMZServerCommon.createInOutSockRef((this).CrushFTPDMZ.this.socks_in_out, sock2);
                                                            CrushFTPDMZ.this.startReceiver(sock2);
                                                            sock2 = null;
                                                            return;
                                                        }
                                                        if (i == 87) {
                                                            Log.log("SERVER", 1, "DMZ WRITE Socket Connected:" + sock2);
                                                            System.out.println(new Date() + "|DMZ WRITE Socket Connected.");
                                                            Common.sockLog(sock2, "DMZ write_sock");
                                                            (this).CrushFTPDMZ.this.write_socks.addElement(sock2);
                                                            DMZServerCommon.createInOutSockRef((this).CrushFTPDMZ.this.socks_in_out, sock2);
                                                            sock2 = null;
                                                            return;
                                                        }
                                                        if (i == 100) {
                                                            Common.sockLog(sock2, "DMZ data_sock:" + (char)i + ":data_sock_available size=" + data_sock_available.size());
                                                            Properties p = new Properties();
                                                            p.put("time", String.valueOf(System.currentTimeMillis()));
                                                            p.put("sock", sock2);
                                                            data_sock_available.insertElementAt(p, 0);
                                                            sock2 = null;
                                                            return;
                                                        }
                                                        if (i == 69) {
                                                            Common.sockLog(sock2, "DMZ proxy sock");
                                                            CrushFTPDMZ.this.proxyConnection(sock2);
                                                            sock2 = null;
                                                            return;
                                                        }
                                                        if (i == 76) {
                                                            Common.sockLog(sock2, "DMZ logging sock");
                                                            CrushFTPDMZ.this.sendLogData(sock2);
                                                            sock2 = null;
                                                            return;
                                                        }
                                                        if (i == 88) {
                                                            Common.sockLog(sock2, "DMZ execute_sock");
                                                            CrushFTPDMZ.this.doExecuteProcess(sock2);
                                                            sock2 = null;
                                                            return;
                                                        }
                                                        if (i == 51 || i == 84 || i == 116 || i == 119 || i == 114) {
                                                            System.out.println(new Date() + "|DMZ Tunnel(" + (char)i + ") Converter Socket Connected.");
                                                            Object object = tunnel_start_lock;
                                                            synchronized (object) {
                                                                if (!System.getProperty("crushftp.dmz.tunnel", "false").equals("false")) return;
                                                                CrushFTPDMZ.this.startupDMZ3(sock2, ss_data_wrapper, command_ip2, i, allowed_ips2);
                                                                return;
                                                            }
                                                        }
                                                        if (i == 52) {
                                                            System.out.println(new Date() + "|DMZ Tunnel(" + (char)i + ") Converter Socket Connected.");
                                                            Object object = tunnel_start_lock;
                                                            synchronized (object) {
                                                                if (!System.getProperty("crushftp.dmz.tunnel", "false").equals("false")) return;
                                                                CrushFTPDMZ.this.startupDMZ4(sock2, ss_data_wrapper, command_ip2, i, allowed_ips2);
                                                                return;
                                                            }
                                                        }
                                                        if (i != 53) return;
                                                        System.out.println(new Date() + "|DMZ Tunnel(" + (char)i + ") Converter Socket Connected.  allowed_ips2:" + allowed_ips2);
                                                        Object object = tunnel_start_lock;
                                                        synchronized (object) {
                                                            if (!System.getProperty("crushftp.dmz.tunnel", "false").equals("false")) return;
                                                            CrushFTPDMZ.this.startupDMZ5(sock2, ss_data_wrapper, command_ip2, i, allowed_ips2);
                                                            return;
                                                        }
                                                    }
                                                    catch (SocketTimeoutException e) {
                                                        if ((this).CrushFTPDMZ.this.started) {
                                                            ++(this).CrushFTPDMZ.this.servers_stopped_count;
                                                            if ((this).CrushFTPDMZ.this.servers_stopped_count > 2 && !(this).CrushFTPDMZ.this.servers_stopped) {
                                                                if (ServerStatus.BG("stop_dmz_ports_internal_down")) {
                                                                    ServerStatus.thisObj.stop_all_servers_including_serverbeat();
                                                                    (this).CrushFTPDMZ.this.servers_stopped = true;
                                                                }
                                                            }
                                                        }
                                                        if (sock2 == null) return;
                                                        Common.sockLog(sock2, "DMZ closed");
                                                        try {
                                                            sock2.close();
                                                            return;
                                                        }
                                                        catch (IOException iOException) {}
                                                        return;
                                                    }
                                                    catch (Exception e) {
                                                        if (!(this).CrushFTPDMZ.this.started) {
                                                            e.printStackTrace();
                                                        }
                                                        Log.log("DMZ", 1, e);
                                                        if (sock2 == null) return;
                                                        Common.sockLog(sock2, "DMZ closed");
                                                        try {
                                                            sock2.close();
                                                            return;
                                                        }
                                                        catch (IOException iOException) {}
                                                        return;
                                                    }
                                                }
                                                finally {
                                                    if (sock2 != null) {
                                                        Common.sockLog(sock2, "DMZ closed");
                                                        try {
                                                            sock2.close();
                                                        }
                                                        catch (IOException iOException) {}
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                                catch (SocketTimeoutException e) {
                                    if (!CrushFTPDMZ.this.started) ** GOTO lbl-1000
                                    ++CrushFTPDMZ.this.servers_stopped_count;
                                    if (CrushFTPDMZ.this.servers_stopped_count <= 2 || CrushFTPDMZ.this.servers_stopped) ** GOTO lbl-1000
                                    if (!ServerStatus.BG("stop_dmz_ports_internal_down")) ** GOTO lbl-1000
                                    ServerStatus.thisObj.stop_all_servers_including_serverbeat();
                                    CrushFTPDMZ.this.servers_stopped = true;
                                }
                                catch (IOException e) {
                                    if (("" + e).indexOf("closed") >= 0) {
                                        Thread.sleep(100L);
                                    }
                                    e.printStackTrace();
                                    continue;
                                }
                                break;
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            System.exit(0);
                            return;
                        }
                        ** GOTO lbl-1000
                    }
                });
            }
            while (true) {
                if (queue.size() > 0) {
                    this.sendCommand((Properties)queue.remove(0));
                    continue;
                }
                Thread.sleep(500L);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
            return;
        }
    }

    public void startupDMZ3(Socket sock2, Properties ss_data_wrapper, String command_ip2, int i, String allowed_ips2) throws Exception {
        System.getProperties().put("crushftp.dmz.tunnel", "true");
        System.getProperties().put("crushftp.dmz.ssl", "false");
        System.getProperties().put("crushftp.dmz3.ssl", "true");
        if (i == 116) {
            System.getProperties().put("crushftp.dmz3.ssl", "false");
        }
        System.out.println(new Date() + "|DMZ changing to tunneled mode!  Restarting.");
        if (sock2 != null) {
            Common.sockLog(sock2, "DMZ changing to tunneled mode!  Restarting.");
        }
        ServerSocket ss_data = (ServerSocket)ss_data_wrapper.get("ss_data");
        ss_data_wrapper.put("ss_data", this.getServerSocket("127.0.0.1", this.command_port_main + 1));
        try {
            new Socket(command_ip2, this.command_port_main).close();
        }
        catch (Exception exception) {}
        try {
            while (this.read_socks.size() > 0) {
                DMZServerCommon.closeInOutSockRef(this.socks_in_out, (Socket)this.write_socks.remove(0));
            }
        }
        catch (Exception exception) {}
        try {
            while (this.write_socks.size() > 0) {
                DMZServerCommon.closeInOutSockRef(this.socks_in_out, (Socket)this.write_socks.remove(0));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.dmzt3 != null) {
            this.dmzt3.close();
        }
        this.dmzt3 = new DMZTunnelServer(this.command_ip_main, this.command_port_main, ss_data);
        this.dmzt3.allowed_ips = allowed_ips2;
        int x = 0;
        while (x < 5) {
            try {
                Thread.sleep(1000L);
                this.dmzt3.startup();
                break;
            }
            catch (Exception e) {
                e.printStackTrace();
                if (x >= 4) {
                    throw e;
                }
                ++x;
            }
        }
    }

    public void startupDMZ4(Socket sock2, Properties ss_data_wrapper, final String command_ip2, int i, String allowed_ips2) throws Exception {
        System.getProperties().put("crushftp.dmz.tunnel", "true");
        System.getProperties().put("crushftp.dmz.ssl", "false");
        System.getProperties().put("crushftp.dmz3.ssl", "false");
        System.out.println(new Date() + "|DMZ changing to tunneled v4 mode!  Restarting.");
        if (sock2 != null) {
            Common.sockLog(sock2, "DMZ changing to tunneled mode!  Restarting.");
        }
        ServerSocket ss_data = (ServerSocket)ss_data_wrapper.get("ss_data");
        ss_data_wrapper.put("ss_data", this.getServerSocket("127.0.0.1", this.command_port_main + 1));
        ss_data.close();
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    Common.getSSLSocket(command_ip2, CrushFTPDMZ.this.command_port_main, true).close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    new Socket(command_ip2, CrushFTPDMZ.this.command_port_main).close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
        try {
            while (this.read_socks.size() > 0) {
                DMZServerCommon.closeInOutSockRef(this.socks_in_out, (Socket)this.write_socks.remove(0));
            }
        }
        catch (Exception exception) {}
        try {
            while (this.write_socks.size() > 0) {
                DMZServerCommon.closeInOutSockRef(this.socks_in_out, (Socket)this.write_socks.remove(0));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.dmzt4 != null) {
            this.dmzt4.close();
        }
        this.dmzt4 = new DMZTunnelServerSSH(this.command_ip_main, this.command_port_main);
        DMZTunnelServerSSH.allowed_ips = allowed_ips2;
        int x = 0;
        while (x < 5) {
            try {
                Thread.sleep(1000L);
                this.dmzt4.startup();
                break;
            }
            catch (Exception e) {
                e.printStackTrace();
                if (x >= 4) {
                    throw e;
                }
                ++x;
            }
        }
        sock2.close();
    }

    public void startupDMZ5(Socket sock2, Properties ss_data_wrapper, final String command_ip2, int i, String allowed_ips2) throws Exception {
        System.getProperties().put("crushftp.dmz.tunnel", "true");
        System.getProperties().put("crushftp.dmz.ssl", "false");
        System.getProperties().put("crushftp.dmz3.ssl", "false");
        System.out.println(new Date() + "|DMZ changing to tunneled v5 mode!  Restarting.");
        if (sock2 != null) {
            Common.sockLog(sock2, "DMZ changing to tunneled mode!  Restarting.");
        }
        ServerSocket ss_data = (ServerSocket)ss_data_wrapper.get("ss_data");
        ss_data_wrapper.put("ss_data", this.getServerSocket("127.0.0.1", this.command_port_main + 1));
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    Common.getSSLSocket(command_ip2, CrushFTPDMZ.this.command_port_main, true).close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    new Socket(command_ip2, CrushFTPDMZ.this.command_port_main).close();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
        try {
            while (this.read_socks.size() > 0) {
                DMZServerCommon.closeInOutSockRef(this.socks_in_out, (Socket)this.write_socks.remove(0));
            }
        }
        catch (Exception exception) {}
        try {
            while (this.write_socks.size() > 0) {
                DMZServerCommon.closeInOutSockRef(this.socks_in_out, (Socket)this.write_socks.remove(0));
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.dmzt5 != null) {
            this.dmzt5.close();
        }
        this.dmzt5 = new DMZTunnelServer5(this.command_ip_main, this.command_port_main, ss_data, allowed_ips2);
        int x = 0;
        while (x < 5) {
            try {
                Thread.sleep(1000L);
                this.dmzt5.startup();
                break;
            }
            catch (Exception e) {
                e.printStackTrace();
                if (x >= 4) {
                    throw e;
                }
                ++x;
            }
        }
        sock2.close();
    }

    public ServerSocket getServerSocket(String command_ip, int command_port) throws Exception {
        ServerSocket ss_data = null;
        ss_data = System.getProperty("crushftp.dmz.ssl", "true").equals("true") && System.getProperty("crushftp.dmz3.ssl", "true").equals("true") && System.getProperty("crushftp.dmz.tunnel", "false").equals("false") ? this.common_code.getServerSocket(command_port, command_ip, "builtin", "crushftp", "crushftp", "", false, 2000) : (System.getProperty("crushftp.dmz.tunnel", "false").equals("true") ? new ServerSocket(command_port, 2000, InetAddress.getByName("127.0.0.1")) : new ServerSocket(command_port));
        if (ss_data instanceof SSLServerSocket) {
            ((SSLServerSocket)ss_data).setEnabledProtocols(System.getProperty("crushftp.dmz.tls_versions", "SSLv2Hello,TLSv1,TLSv1.1,TLSv1.2").split(","));
        }
        return ss_data;
    }

    public void doExecuteProcess(Socket sock) {
        Process proc = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
            Properties p = (Properties)ois.readObject();
            Thread.currentThread().setName("DMZ_process_executor:" + sock + ":" + p);
            OutputStream sock2_out = sock.getOutputStream();
            InputStream sock2_in = sock.getInputStream();
            String parent_str = p.getProperty("parent");
            File_S parent = new File_S(parent_str);
            if (parent.isFile()) {
                parent = (File_S)parent.getParentFile();
            }
            Common.check_exec();
            proc = Runtime.getRuntime().exec((String[])p.get("commandArgs"), (String[])p.get("env"), (File)parent);
            InputStream proc_in = proc.getInputStream();
            OutputStream proc_out = proc.getOutputStream();
            InputStream proc_error = proc.getErrorStream();
            Common.streamCopier(null, null, proc_in, sock2_out, true, false, false);
            Common.streamCopier(null, null, proc_error, sock2_out, true, false, false);
            Common.streamCopier(null, null, sock2_in, proc_out, true, false, false);
            proc.waitFor();
            int exit_val = proc.exitValue();
            sock2_out.write(("\r\n" + exit_val + "\r\n").getBytes("UTF8"));
            sock2_out.flush();
            proc.getOutputStream().close();
            proc_in.close();
            proc_error.close();
            sock.close();
            proc = null;
        }
        catch (Exception e) {
            Log.log("DMZ", 0, e);
        }
        if (proc != null) {
            proc.destroy();
        }
    }

    public void proxyConnection(Socket sock) throws Exception {
        Socket sock2 = null;
        String host_port = "";
        try {
            Common.sockLog(sock, "DMZ external_sock");
            int len = sock.getInputStream().read();
            byte[] b = new byte[len];
            while (len > 0) {
                int len2 = sock.getInputStream().read(b, b.length - len, len);
                if (len2 < 0) {
                    len = len2;
                    continue;
                }
                len -= len2;
            }
            host_port = new String(b, "UTF8");
            Log.log("DMZ", 0, "CONNECTING:Proxying outgoing connection from internal server (" + sock + ") to:" + host_port);
            sock2 = new Socket(host_port.split(":")[0], Integer.parseInt(host_port.split(":")[1]));
            Log.log("DMZ", 0, "SUCCESS:Proxyied outgoing connection from internal server (" + sock + ") to:" + host_port);
            Common.sockLog(sock2, "DMZ external_sock2");
            sock2.setTcpNoDelay(true);
            sock.setSoTimeout(600000);
            sock2.setSoTimeout(600000);
            Common.streamCopier(sock, sock2, sock.getInputStream(), sock2.getOutputStream(), true, true, true);
            Common.streamCopier(sock, sock2, sock2.getInputStream(), sock.getOutputStream(), true, true, true);
        }
        catch (Exception e) {
            Log.log("DMZ", 0, "Proxy sock error:" + sock + "->" + sock2 + ":" + host_port + ":" + e);
            try {
                sock.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
    }

    public void sendLogData(Socket sock) throws Exception {
        if (!ServerStatus.BG("dmz_log_in_internal_server")) {
            sock.close();
        }
        try {
            Common.sockLog(sock, "DMZ logging socket");
            ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
            while (true) {
                if (Log.dmz_log_queue.size() > 0) {
                    Properties p = (Properties)Log.dmz_log_queue.remove(0);
                    oos.writeObject(p);
                    oos.flush();
                    oos.reset();
                    continue;
                }
                Thread.sleep(1000L);
            }
        }
        catch (Exception e) {
            Log.log("DMZ", 0, "Logging socket error:" + e);
            try {
                sock.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
            return;
        }
    }

    public void closeExpiredDataSocks() throws Exception {
        Worker.startWorker(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                Thread.currentThread().setName("DMZDataSocketsExpire");
                while (!System.getProperty("crushftp.dmz.tunnel", "false").equals("true")) {
                    try {
                        Object object = data_sock_available_lock;
                        synchronized (object) {
                            int x = data_sock_available.size() - 1;
                            while (x >= 0) {
                                Properties p = (Properties)data_sock_available.elementAt(x);
                                long time = Long.parseLong(p.getProperty("time"));
                                if (System.currentTimeMillis() - time > (long)(DMZServerCommon.MAX_DMZ_SOCKET_IDLE_TIME - 2000)) {
                                    data_sock_available.remove(p);
                                    Socket sock = (Socket)p.remove("sock");
                                    Common.sockLog(sock, "data sock expired, closing.  data_sock_available size=" + data_sock_available.size());
                                    try {
                                        sock.close();
                                    }
                                    catch (IOException iOException) {
                                        // empty catch block
                                    }
                                }
                                --x;
                            }
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException interruptedException) {}
                }
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void sendCommand(Properties p) {
        block15: {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos);
                out.reset();
                try {
                    Object object = SharedSession.sessionLock;
                    synchronized (object) {
                        out.writeObject(p);
                        out.close();
                    }
                }
                catch (Exception e) {
                    System.out.println(new Date());
                    e.printStackTrace();
                    System.out.println(new Date());
                    Log.log("SERVER", 1, "Skipping write on object due to serialization exception.");
                    Log.log("SERVER", 1, e);
                    return;
                }
                byte[] b = baos.toByteArray();
                boolean wrote = false;
                if (p.getProperty("need_response", "false").equalsIgnoreCase("true")) {
                    p.put("status", "waiting");
                    dmzResponses.put(p.getProperty("id"), p);
                }
                int x = this.write_socks.size() - 1;
                while (x >= 0) {
                    Socket sock = (Socket)this.write_socks.elementAt(x);
                    sock.setSoTimeout(10000);
                    Properties in_out = (Properties)this.socks_in_out.get(sock);
                    DataInputStream din = (DataInputStream)in_out.get("in");
                    DataOutputStream dout = (DataOutputStream)in_out.get("out");
                    try {
                        long start = System.currentTimeMillis();
                        dout.writeInt(b.length);
                        dout.write(b);
                        dout.flush();
                        int i = din.readInt();
                        if (i != b.length) {
                            throw new IOException(String.valueOf(i) + " versus " + b.length + "  " + sock);
                        }
                        wrote = true;
                        Thread.currentThread().setName("DMZSender:queue=" + queue.size() + " last write len=" + b.length + "(" + p.getProperty("type") + ") milliseconds=" + (System.currentTimeMillis() - start));
                    }
                    catch (IOException e) {
                        Common.sockLog(sock, "writing command error:" + e);
                        DMZServerCommon.closeInOutSockRef(this.socks_in_out, sock);
                        this.write_socks.remove(sock);
                        Log.log("DMZ", 0, "Removed dead socket:" + sock + ":" + e);
                        Log.log("DMZ", 0, e);
                    }
                    --x;
                }
                if (wrote) {
                    if (!p.getProperty("type").equals("PUT:LOGGING")) {
                        Log.log("DMZ", 1, "WROTE:" + p.getProperty("type") + ":" + p.getProperty("id"));
                    }
                    break block15;
                }
                Log.log("DMZ", 1, "FAILED WRITE:" + p.getProperty("type") + ":" + p.getProperty("id"));
                throw new Exception("Unable to write DMZ message, no server to write to:" + this.write_socks + " servers_stopped_count=" + this.servers_stopped_count + " servers_stopped=" + this.servers_stopped);
            }
            catch (Exception e) {
                System.out.println(new Date());
                e.printStackTrace();
            }
        }
    }

    private void startReceiver(Socket read_sock) {
        Thread.currentThread().setName("DMZResponseSocketReader:" + read_sock);
        try {
            Properties in_out = (Properties)this.socks_in_out.get(read_sock);
            DataInputStream din = (DataInputStream)in_out.get("in");
            DataOutputStream dout = (DataOutputStream)in_out.get("out");
            while (true) {
                try {
                    while (true) {
                        int len = din.readInt();
                        byte[] b = new byte[len];
                        int bytesRead = 0;
                        int totalBytes = 0;
                        Common.sockLog(read_sock, "read_sock got command:" + len);
                        while (totalBytes < len) {
                            bytesRead = din.read(b, totalBytes, len - totalBytes);
                            if (bytesRead < 0) {
                                throw new Exception("DMZ:EOF reached in receiver read of chunk.");
                            }
                            totalBytes += bytesRead;
                        }
                        dout.writeInt(totalBytes);
                        dout.flush();
                        if (len <= 0) continue;
                        Common.sockLog(read_sock, "read_sock got command complete:" + len);
                        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
                        Properties p = (Properties)ois.readObject();
                        ois.close();
                        this.processResponse(p);
                    }
                }
                catch (SocketTimeoutException socketTimeoutException) {
                    continue;
                }
                break;
            }
        }
        catch (Exception e) {
            System.out.println(new Date());
            e.printStackTrace();
            System.out.println(new Date());
            return;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static void reset_data_sockets() throws Exception {
        Object object = data_sock_available_lock;
        synchronized (object) {
            System.out.println(new Date() + ":Clearing out all old data sockets and starting fresh:" + data_sock_available.size());
            Log.log("DMZ", 1, "Clearing out all old data sockets and starting fresh:" + data_sock_available.size());
            while (data_sock_available.size() > 0) {
                Properties pp = (Properties)data_sock_available.remove(0);
                ((Socket)pp.remove("sock")).close();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void processResponse(Properties p) throws Exception {
        Cloneable response;
        if (dmzResponses.containsKey(((Properties)p).getProperty("id", ""))) {
            response = (Properties)dmzResponses.remove(((Properties)p).getProperty("id"));
            if (response == null) {
                return;
            }
            Log.log("DMZ", 0, "READ:RECEIVED_RESPONSE:" + ((Properties)response).getProperty("type") + ":" + ((Properties)p).getProperty("id"));
            ((Properties)response).putAll((Map<?, ?>)((Object)p));
            if (((Properties)p).containsKey("data")) {
                ((Properties)response).putAll((Map<?, ?>)((Properties)((Properties)p).get("data")));
            }
            p = response;
            ((Properties)p).put("status", "done");
        }
        Log.log("DMZ", 1, "READ:" + ((Properties)p).getProperty("type") + ":" + ((Properties)p).getProperty("id"));
        if (((Properties)p).getProperty("type").equalsIgnoreCase("PUT:SERVER_SETTINGS")) {
            response = queue;
            synchronized (response) {
                if (!this.started) {
                    this.starting = true;
                    System.out.println(new Date() + "|DMZ Starting...");
                    try {
                        Properties server_settings2 = (Properties)((Properties)p).get("data");
                        new ServerStatus(true, server_settings2);
                        System.out.println(new Date() + "|DMZ Started");
                    }
                    catch (Exception e) {
                        System.out.println(new Date() + "|DMZ Error");
                        e.printStackTrace();
                        System.exit(0);
                    }
                    this.started = true;
                } else {
                    crushftp.handlers.Common.updateObjectLog((Properties)((Properties)p).get("data"), ServerStatus.server_settings, null);
                    System.out.println(new Date() + "|DMZ Re-started");
                    CrushFTPDMZ.reset_data_sockets();
                }
            }
            ((Properties)p).put("data", new Properties());
            ((Properties)p).put("type", "PUT:DMZ_STARTED");
            queue.addElement(p);
        } else if (((Properties)p).getProperty("type").equalsIgnoreCase("PUT:SYSTEM.PROPERTIES")) {
            Properties system_prop = (Properties)((Properties)p).get("data");
            Log.log("DMZ", 1, "READ:" + system_prop);
            Common.System2.put(system_prop.getProperty("key"), system_prop.get("val"));
        } else if (((Properties)p).getProperty("type").equalsIgnoreCase("GET:SERVER_SETTINGS")) {
            ((Properties)p).put("data", ServerStatus.server_settings);
            ((Properties)p).put("type", "RESPONSE");
            queue.addElement(p);
        } else if (((Properties)p).getProperty("type").equalsIgnoreCase("GET:SERVER_INFO")) {
            Properties request = (Properties)((Properties)p).get("data");
            Properties si = new Properties();
            if (this.started) {
                si = (Properties)ServerStatus.thisObj.server_info.clone();
            }
            si.remove("plugins");
            if (request != null && (request.getProperty("key", "").equals("server_info") || request.getProperty("command", "").equals("getStatHistory"))) {
                si.remove("user_list");
                si.remove("recent_user_list");
            } else if (si.get("user_list") != null) {
                Vector user_list = (Vector)((Vector)si.get("user_list")).clone();
                si.put("user_list", user_list);
                int x = 0;
                while (x < user_list.size()) {
                    Properties user_info = (Properties)((Properties)user_list.elementAt(x)).clone();
                    user_list.setElementAt(user_info, x);
                    user_info.remove("session");
                    ++x;
                }
            }
            ((Properties)p).put("data", si);
            ((Properties)p).put("type", "RESPONSE");
            queue.addElement(p);
        } else if (((Properties)p).getProperty("type").equalsIgnoreCase("RUN:INSTANCE_ACTION")) {
            ((Properties)p).put("data", AdminControls.runInstanceAction((Properties)((Properties)p).get("data"), ((Properties)p).getProperty("site"), "127.0.0.1"));
            ((Properties)p).put("type", "RESPONSE");
            queue.addElement(p);
        } else if (((Properties)p).getProperty("type").equalsIgnoreCase("RUN:JOB")) {
            Log.log("SERVER", 0, "READ:" + ((Properties)p).getProperty("type") + ":" + ((Properties)p).getProperty("id"));
            Cloneable p_f2 = p;
            final Properties info = (Properties)((Properties)p).remove("data");
            Worker.startWorker(new Runnable((Properties)p_f2){
                private final /* synthetic */ Properties val$p_f2;
                {
                    this.val$p_f2 = properties2;
                }

                @Override
                public void run() {
                    Vector items = (Vector)info.remove("items");
                    Properties event = (Properties)info.remove("data");
                    event.put("event_plugin_list", "CrushTask (User Defined)");
                    event.put("name", event.getProperty("scheduleName"));
                    Log.log("SERVER", 0, "READ:" + this.val$p_f2.getProperty("type") + ":" + this.val$p_f2.getProperty("id") + ":" + event.getProperty("name"));
                    Properties info2 = ServerStatus.thisObj.events6.doEventPlugin(null, event, null, items);
                    this.val$p_f2.put("data", info2);
                    byte[] b = null;
                    try {
                        RandomAccessFile raf = new RandomAccessFile(new File_S(info2.getProperty("log_file")), "r");
                        b = new byte[(int)raf.length()];
                        raf.readFully(b);
                        raf.close();
                    }
                    catch (Throwable e) {
                        Log.log("DMZ", 0, e);
                        b = new byte[]{};
                    }
                    this.val$p_f2.put("log", new String(b));
                    this.val$p_f2.put("type", "RESPONSE");
                    queue.addElement(this.val$p_f2);
                    Log.log("SERVER", 0, "READ:" + this.val$p_f2.getProperty("type") + ":" + this.val$p_f2.getProperty("id") + ":" + event.getProperty("name") + ":complete");
                }
            });
        } else if (((Properties)p).getProperty("type").equalsIgnoreCase("PUT:PING")) {
            Properties pong = (Properties)((Properties)p).remove("data");
            pong.put("time2", String.valueOf(System.currentTimeMillis()));
            ((Properties)p).put("data", pong);
            ((Properties)p).put("type", "PUT:PONG");
            queue.insertElementAt(p, 0);
        }
    }
}

