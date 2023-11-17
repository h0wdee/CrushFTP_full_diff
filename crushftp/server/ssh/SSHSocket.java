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
import java.net.Socket;
import java.util.Date;
import java.util.Properties;

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
        block24: {
            String user_ip = this.p.getProperty("user_ip");
            this.sockIn = (Socket)this.p.get("socket");
            this.p.put("socket", this);
            Properties server_item = (Properties)this.p.get("server_item");
            ServerStatus.thisObj.append_log(String.valueOf(ServerStatus.thisObj.logDateFormat.format(new Date())) + "|" + "[SFTP:" + server_item.getProperty("ip", "0.0.0.0") + ":" + server_item.getProperty("port", "21") + "][" + this.p.getProperty("user_number") + "] " + LOC.G("Accepting connection from") + ": " + user_ip + ":" + this.sockIn.getPort() + "\r\n", "ACCEPT");
            int localPort = 0;
            try {
                try {
                    BufferedInputStream bis = new BufferedInputStream(this.sockIn.getInputStream());
                    Object object = socket_lock;
                    synchronized (object) {
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
                catch (Exception e) {
                    Log.log("SSH_SERVER", 1, e);
                    ServerStatus.thisObj.append_log("[SFTP:" + server_item.getProperty("ip", "0.0.0.0") + ":" + server_item.getProperty("port", "21") + "][" + this.p.getProperty("user_number") + "] " + LOC.G("Disconnected") + ": " + user_ip + ":" + this.sockIn.getPort() + "\r\n", "QUIT");
                    Log.log("SSH_SERVER", 2, "SSH PORT CONNECTOR close RELEASE:" + localPort);
                    ServerSessionSSH.connectionLookup.remove(String.valueOf(localPort));
                    SSHCrushAuthentication8.endSession((SessionCrush)this.p.get("session"));
                    if (this.p.get("session_id") != null) {
                        ServerSessionSSH.sessionLookup.remove(this.p.get("session_id"));
                        SSHServerSessionFactory.scp_fs.remove(this.p.get("session_id"));
                    }
                    this.p.clear();
                    GenericServer genericServer = this.server;
                    synchronized (genericServer) {
                        --this.server.connected_users;
                        if (this.server.connected_users < 0) {
                            this.server.connected_users = 0;
                        }
                    }
                    QuickConnect.remove_ip_count(user_ip);
                    break block24;
                }
            }
            catch (Throwable throwable) {
                ServerStatus.thisObj.append_log("[SFTP:" + server_item.getProperty("ip", "0.0.0.0") + ":" + server_item.getProperty("port", "21") + "][" + this.p.getProperty("user_number") + "] " + LOC.G("Disconnected") + ": " + user_ip + ":" + this.sockIn.getPort() + "\r\n", "QUIT");
                Log.log("SSH_SERVER", 2, "SSH PORT CONNECTOR close RELEASE:" + localPort);
                ServerSessionSSH.connectionLookup.remove(String.valueOf(localPort));
                SSHCrushAuthentication8.endSession((SessionCrush)this.p.get("session"));
                if (this.p.get("session_id") != null) {
                    ServerSessionSSH.sessionLookup.remove(this.p.get("session_id"));
                    SSHServerSessionFactory.scp_fs.remove(this.p.get("session_id"));
                }
                this.p.clear();
                GenericServer genericServer = this.server;
                synchronized (genericServer) {
                    --this.server.connected_users;
                    if (this.server.connected_users < 0) {
                        this.server.connected_users = 0;
                    }
                }
                QuickConnect.remove_ip_count(user_ip);
                throw throwable;
            }
            ServerStatus.thisObj.append_log("[SFTP:" + server_item.getProperty("ip", "0.0.0.0") + ":" + server_item.getProperty("port", "21") + "][" + this.p.getProperty("user_number") + "] " + LOC.G("Disconnected") + ": " + user_ip + ":" + this.sockIn.getPort() + "\r\n", "QUIT");
            Log.log("SSH_SERVER", 2, "SSH PORT CONNECTOR close RELEASE:" + localPort);
            ServerSessionSSH.connectionLookup.remove(String.valueOf(localPort));
            SSHCrushAuthentication8.endSession((SessionCrush)this.p.get("session"));
            if (this.p.get("session_id") != null) {
                ServerSessionSSH.sessionLookup.remove(this.p.get("session_id"));
                SSHServerSessionFactory.scp_fs.remove(this.p.get("session_id"));
            }
            this.p.clear();
            GenericServer genericServer = this.server;
            synchronized (genericServer) {
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
                block42: {
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
                        catch (Exception e) {
                            if (e.getMessage() == null || !e.getMessage().equalsIgnoreCase("Socket closed") && !e.getMessage().equalsIgnoreCase("Connection reset")) {
                                Log.log("SERVER", 2, e);
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
                            if (!closeInput || !closeOutput) break block42;
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

