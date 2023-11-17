/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.ssh;

import com.crushftp.client.Common;
import com.crushftp.client.Worker;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.server.QuickConnect;
import crushftp.server.ServerSessionSSH;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.GenericServer;
import crushftp.server.ssh.SSHCrushAuthentication8;
import crushftp.server.ssh.SSHServerSessionFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class SSHSocket
extends Socket {
    public Socket sockIn = null;
    Properties p = null;
    Socket sockOut = new Socket();
    Socket sockOut2 = null;
    GenericServer server = null;
    int localSSHPort = 0;
    public static transient Object socket_lock = new Object();

    public SSHSocket(GenericServer server, Properties p, int localSSHPort) {
        this.p = p;
        this.server = server;
        this.localSSHPort = localSSHPort;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run() {
        block50: {
            Object object;
            String user_ip = this.p.getProperty("user_ip");
            this.sockIn = (Socket)this.p.get("socket");
            this.p.put("socket", this);
            Properties server_item = (Properties)this.p.get("server_item");
            ServerStatus.thisObj.append_log(String.valueOf(ServerStatus.thisObj.logDateFormat.format(new Date())) + "|" + "[SFTP:" + server_item.getProperty("ip", "0.0.0.0") + ":" + server_item.getProperty("port", "21") + "][" + this.p.getProperty("user_number") + "] " + LOC.G("Accepting connection from") + ": " + user_ip + ":" + this.sockIn.getPort() + "\r\n", "ACCEPT");
            int localPort = 0;
            try {
                BufferedInputStream bis = new BufferedInputStream(this.sockIn.getInputStream());
                Object object2 = socket_lock;
                synchronized (object2) {
                    this.sockOut2 = new Socket("127.0.0.1", this.localSSHPort);
                }
                this.sockOut.connect(this.sockOut2.getRemoteSocketAddress());
                localPort = this.sockOut.getLocalPort();
                ServerSessionSSH.connectionLookup.put(String.valueOf(localPort), this.p);
                Log.log("SSH_SERVER", 2, "SSH PORT CONNECTOR:" + localPort);
                if (ServerStatus.BG("thread_dump_delayed_login")) {
                    SSHSocket.thread_dump_delayed_login(this.p);
                }
                this.sockIn.setSoTimeout(Integer.parseInt(server_item.getProperty("ssh_session_timeout", "300")) * 1000);
                this.sockOut.setSoTimeout(Integer.parseInt(server_item.getProperty("ssh_session_timeout", "300")) * 1000);
                SSHSocket.streamCopier(this.sockIn, this.sockOut, bis, this.sockOut.getOutputStream(), true, true, true, this.p);
                SSHSocket.streamCopier(this.sockIn, this.sockOut, this.sockOut.getInputStream(), this.sockIn.getOutputStream(), false, true, true, this.p);
                Thread.sleep(100L);
            }
            catch (ConnectException e) {
                Object object3;
                Log.log("SSH_SERVER", 1, e);
                Log.log("SSH_SERVER", 1, "SFTP port is not actually running...telling parent to restart port...");
                try {
                    this.server.server_sock.close();
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                int i = ServerStatus.thisObj.main_servers.indexOf(this.server);
                ServerStatus.thisObj.stop_this_server(i);
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                ServerStatus.thisObj.start_this_server(i);
                ServerStatus.thisObj.append_log("[SFTP:" + server_item.getProperty("ip", "0.0.0.0") + ":" + server_item.getProperty("port", "21") + "][" + this.p.getProperty("user_number") + "] " + LOC.G("Disconnected") + ": " + user_ip + ":" + this.sockIn.getPort() + "\r\n", "QUIT");
                Log.log("SSH_SERVER", 2, "SSH PORT CONNECTOR close RELEASE:" + localPort);
                ServerSessionSSH.connectionLookup.remove(String.valueOf(localPort));
                SSHCrushAuthentication8.endSession((SessionCrush)this.p.get("session"));
                if (this.p.get("session_id") != null) {
                    ServerSessionSSH.sessionLookup.remove(this.p.get("session_id"));
                    SSHServerSessionFactory.scp_fs.remove(this.p.get("session_id"));
                    object3 = ServerSessionSSH.cross_session_lookup;
                    synchronized (object3) {
                        Vector connected_channels = (Vector)ServerSessionSSH.cross_session_lookup.get(this.p.get("session_id"));
                        if (connected_channels != null) {
                            connected_channels.removeAllElements();
                            ServerSessionSSH.cross_session_lookup.remove(this.p.get("session_id"));
                        }
                    }
                }
                this.p.clear();
                object3 = this.server;
                synchronized (object3) {
                    --this.server.connected_users;
                    if (this.server.connected_users < 0) {
                        this.server.connected_users = 0;
                    }
                }
                QuickConnect.remove_ip_count(user_ip);
                break block50;
            }
            catch (Exception e) {
                Object object4;
                try {
                    Log.log("SSH_SERVER", 1, e);
                    ServerStatus.thisObj.append_log("[SFTP:" + server_item.getProperty("ip", "0.0.0.0") + ":" + server_item.getProperty("port", "21") + "][" + this.p.getProperty("user_number") + "] " + LOC.G("Disconnected") + ": " + user_ip + ":" + this.sockIn.getPort() + "\r\n", "QUIT");
                }
                catch (Throwable throwable) {
                    Object object5;
                    ServerStatus.thisObj.append_log("[SFTP:" + server_item.getProperty("ip", "0.0.0.0") + ":" + server_item.getProperty("port", "21") + "][" + this.p.getProperty("user_number") + "] " + LOC.G("Disconnected") + ": " + user_ip + ":" + this.sockIn.getPort() + "\r\n", "QUIT");
                    Log.log("SSH_SERVER", 2, "SSH PORT CONNECTOR close RELEASE:" + localPort);
                    ServerSessionSSH.connectionLookup.remove(String.valueOf(localPort));
                    SSHCrushAuthentication8.endSession((SessionCrush)this.p.get("session"));
                    if (this.p.get("session_id") != null) {
                        ServerSessionSSH.sessionLookup.remove(this.p.get("session_id"));
                        SSHServerSessionFactory.scp_fs.remove(this.p.get("session_id"));
                        object5 = ServerSessionSSH.cross_session_lookup;
                        synchronized (object5) {
                            Vector connected_channels = (Vector)ServerSessionSSH.cross_session_lookup.get(this.p.get("session_id"));
                            if (connected_channels != null) {
                                connected_channels.removeAllElements();
                                ServerSessionSSH.cross_session_lookup.remove(this.p.get("session_id"));
                            }
                        }
                    }
                    this.p.clear();
                    object5 = this.server;
                    synchronized (object5) {
                        --this.server.connected_users;
                        if (this.server.connected_users < 0) {
                            this.server.connected_users = 0;
                        }
                    }
                    QuickConnect.remove_ip_count(user_ip);
                    throw throwable;
                }
                Log.log("SSH_SERVER", 2, "SSH PORT CONNECTOR close RELEASE:" + localPort);
                ServerSessionSSH.connectionLookup.remove(String.valueOf(localPort));
                SSHCrushAuthentication8.endSession((SessionCrush)this.p.get("session"));
                if (this.p.get("session_id") != null) {
                    ServerSessionSSH.sessionLookup.remove(this.p.get("session_id"));
                    SSHServerSessionFactory.scp_fs.remove(this.p.get("session_id"));
                    object4 = ServerSessionSSH.cross_session_lookup;
                    synchronized (object4) {
                        Vector connected_channels = (Vector)ServerSessionSSH.cross_session_lookup.get(this.p.get("session_id"));
                        if (connected_channels != null) {
                            connected_channels.removeAllElements();
                            ServerSessionSSH.cross_session_lookup.remove(this.p.get("session_id"));
                        }
                    }
                }
                this.p.clear();
                object4 = this.server;
                synchronized (object4) {
                    --this.server.connected_users;
                    if (this.server.connected_users < 0) {
                        this.server.connected_users = 0;
                    }
                }
                QuickConnect.remove_ip_count(user_ip);
                break block50;
            }
            ServerStatus.thisObj.append_log("[SFTP:" + server_item.getProperty("ip", "0.0.0.0") + ":" + server_item.getProperty("port", "21") + "][" + this.p.getProperty("user_number") + "] " + LOC.G("Disconnected") + ": " + user_ip + ":" + this.sockIn.getPort() + "\r\n", "QUIT");
            Log.log("SSH_SERVER", 2, "SSH PORT CONNECTOR close RELEASE:" + localPort);
            ServerSessionSSH.connectionLookup.remove(String.valueOf(localPort));
            SSHCrushAuthentication8.endSession((SessionCrush)this.p.get("session"));
            if (this.p.get("session_id") != null) {
                ServerSessionSSH.sessionLookup.remove(this.p.get("session_id"));
                SSHServerSessionFactory.scp_fs.remove(this.p.get("session_id"));
                object = ServerSessionSSH.cross_session_lookup;
                synchronized (object) {
                    Vector connected_channels = (Vector)ServerSessionSSH.cross_session_lookup.get(this.p.get("session_id"));
                    if (connected_channels != null) {
                        connected_channels.removeAllElements();
                        ServerSessionSSH.cross_session_lookup.remove(this.p.get("session_id"));
                    }
                }
            }
            this.p.clear();
            object = this.server;
            synchronized (object) {
                --this.server.connected_users;
                if (this.server.connected_users < 0) {
                    this.server.connected_users = 0;
                }
            }
            QuickConnect.remove_ip_count(user_ip);
        }
    }

    @Override
    public void close() throws IOException {
        this.sockIn.close();
        this.sockOut.close();
        if (this.sockOut2 != null) {
            this.sockOut2.close();
        }
        super.close();
    }

    public static void streamCopier(final Socket sock1, final Socket sock2, final InputStream in, final OutputStream out, boolean async, final boolean closeInput, final boolean closeOutput, final Properties p) throws InterruptedException {
        Runnable r = new Runnable(){

            @Override
            public void run() {
                block57: {
                    Properties server_item = (Properties)p.get("server_item");
                    SessionCrush thisSession = null;
                    InputStream inp = in;
                    OutputStream outp = out;
                    try {
                        try {
                            byte[] b = new byte[65535];
                            int bytesRead = 0;
                            while (bytesRead >= 0) {
                                bytesRead = inp.read(b);
                                if (bytesRead >= 0) {
                                    outp.write(b, 0, bytesRead);
                                }
                                if (thisSession == null) {
                                    thisSession = (SessionCrush)p.get("session");
                                }
                                if (thisSession == null) continue;
                                thisSession.active();
                            }
                        }
                        catch (SocketTimeoutException e) {
                            if (e.getMessage() == null || !e.getMessage().equalsIgnoreCase("Socket closed") && !e.getMessage().equalsIgnoreCase("Connection reset")) {
                                Log.log("SERVER", 2, e);
                            }
                            if (thisSession != null) {
                                thisSession.add_log("[" + server_item.getProperty("serverType", "ftp") + ":" + thisSession.uiSG("user_number") + ":" + thisSession.uiSG("user_name") + ":" + thisSession.uiSG("user_ip") + "] " + "SFTP session timeout:" + server_item.getProperty("ssh_session_timeout", "300") + " seconds", "USER");
                            }
                            if (closeInput) {
                                try {
                                    inp.close();
                                }
                                catch (Exception e2) {
                                    Log.log("SERVER", 1, e2);
                                }
                            }
                            if (closeOutput) {
                                try {
                                    outp.close();
                                }
                                catch (Exception e3) {
                                    Log.log("SERVER", 1, e3);
                                }
                            }
                            if (!closeInput || !closeOutput) break block57;
                            try {
                                if (sock1 != null) {
                                    sock1.close();
                                }
                            }
                            catch (Exception e3) {
                                // empty catch block
                            }
                            try {
                                if (sock2 != null) {
                                    sock2.close();
                                }
                            }
                            catch (Exception e3) {}
                        }
                        catch (Exception e) {
                            block58: {
                                if (e.getMessage() == null || !e.getMessage().equalsIgnoreCase("Socket closed") && !e.getMessage().equalsIgnoreCase("Connection reset")) {
                                    Log.log("SERVER", 2, e);
                                }
                                if (!closeInput) break block58;
                                try {
                                    inp.close();
                                }
                                catch (Exception e4) {
                                    Log.log("SERVER", 1, e4);
                                }
                            }
                            if (closeOutput) {
                                try {
                                    outp.close();
                                }
                                catch (Exception e5) {
                                    Log.log("SERVER", 1, e5);
                                }
                            }
                            if (!closeInput || !closeOutput) break block57;
                            try {
                                if (sock1 != null) {
                                    sock1.close();
                                }
                            }
                            catch (Exception e5) {
                                // empty catch block
                            }
                            try {
                                if (sock2 != null) {
                                    sock2.close();
                                }
                            }
                            catch (Exception e5) {}
                        }
                    }
                    finally {
                        if (closeInput) {
                            try {
                                inp.close();
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 1, e);
                            }
                        }
                        if (closeOutput) {
                            try {
                                outp.close();
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 1, e);
                            }
                        }
                        if (closeInput && closeOutput) {
                            try {
                                if (sock1 != null) {
                                    sock1.close();
                                }
                            }
                            catch (Exception exception) {}
                            try {
                                if (sock2 != null) {
                                    sock2.close();
                                }
                            }
                            catch (Exception exception) {}
                        }
                    }
                }
            }
        };
        try {
            if (async) {
                Worker.startWorker(r);
            } else {
                r.run();
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
    }

    public static void thread_dump_delayed_login(final Properties p) {
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    int loops = 0;
                    while (loops++ < 6) {
                        try {
                            SessionCrush thisSession;
                            Thread.sleep(5000L);
                            if (p.containsKey("session") && (thisSession = (SessionCrush)p.get("session")).uiBG("user_logged_in") && thisSession.uiBG("sftp_login_complete") || p.size() == 0) break;
                            if (loops < 3) continue;
                            String result = Common.dumpStack("SLOW LOGIN:" + ServerStatus.version_info_str + ServerStatus.sub_version_info_str);
                            Properties info = new Properties();
                            info.put("alert_timeout", "0");
                            info.put("alert_max", "0");
                            info.put("alert_msg", result);
                            ServerStatus.thisObj.runAlerts("slow_login", info, null, null);
                            Log.log("SERVER", 2, result);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }
            });
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }
}

