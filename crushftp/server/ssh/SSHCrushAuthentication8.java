/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.sshd.Connection
 *  com.maverick.sshd.ConnectionManager
 */
package crushftp.server.ssh;

import com.crushftp.client.Worker;
import com.maverick.sshd.Connection;
import com.maverick.sshd.ConnectionManager;
import crushftp.gui.LOC;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.server.QuickConnect;
import crushftp.server.ServerSessionSSH;
import crushftp.server.ServerStatus;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Properties;

public class SSHCrushAuthentication8 {
    public int getAuthenticationStatus() {
        return -1;
    }

    public static SessionCrush getSession(String sessionid) {
        SessionCrush thisSession;
        Properties p;
        Connection conn = ConnectionManager.getInstance().getConnectionById(sessionid);
        String username = conn.getUsername();
        if (ServerStatus.BG("username_uppercase")) {
            username = username.toUpperCase();
        }
        if (ServerStatus.BG("lowercase_usernames")) {
            username = username.toLowerCase();
        }
        int loops = 0;
        String login_info = "SSH PORT CONNECTOR LOOKUP:" + conn.getRemotePort() + " Cipher CS/SC:" + conn.getCipherCS() + "/" + conn.getCipherSC() + "KEX:" + conn.getKeyEchangeInUse() + " Mac CS/SC:" + conn.getMacCS() + "/" + conn.getMacSC() + " Client:" + conn.getRemoteIdentification();
        Log.log("SSH_SERVER", 0, login_info);
        int idle_time = ServerStatus.IG("sftp_login_timeout_max") * 1000;
        while (loops++ < idle_time) {
            p = (Properties)ServerSessionSSH.connectionLookup.get(String.valueOf(conn.getRemotePort()));
            if (p != null && System.currentTimeMillis() - Long.parseLong(p.getProperty("connectionTime", "0")) < 10000L) break;
            try {
                Thread.sleep(1L);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (loops >= idle_time) {
            Log.log("SSH_SERVER", 0, "Timeout waiting for login to be completed reached! " + idle_time + "ms, " + login_info);
            throw new RuntimeException("Timeout waiting for login to be completed reached! " + idle_time + "ms, " + login_info);
        }
        p = (Properties)ServerSessionSSH.connectionLookup.get(String.valueOf(conn.getRemotePort()));
        Log.log("SSH_SERVER", 2, "SSH PORT CONNECTOR LOOKUP:" + conn.getRemotePort() + ":" + p);
        Properties server_item = (Properties)p.get("server_item");
        try {
            if (!QuickConnect.validate_ip(p.getProperty("user_ip"), server_item).equals("")) {
                Log.log("SERVER", 0, "SFTP Session banned IP:" + p.getProperty("user_ip") + ":" + QuickConnect.validate_ip(p.getProperty("user_ip"), server_item));
                Socket sock = (Socket)p.remove("socket");
                if (sock != null) {
                    sock.close();
                }
                return null;
            }
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 0, e);
        }
        if ((thisSession = (SessionCrush)ServerSessionSSH.sessionLookup.get(conn.getSessionId())) == null) {
            thisSession = new SessionCrush((Socket)p.get("socket"), Integer.parseInt(p.getProperty("user_number")), p.getProperty("user_ip"), Integer.parseInt(p.getProperty("user_port")), p.getProperty("listen_ip"), p.getProperty("listen_ip_port"), server_item);
            thisSession.put("connection_info", String.valueOf(conn.getRemoteIdentification().trim()) + " CipherCS:" + conn.getCipherCS().trim() + " CipherSC:" + conn.getCipherSC().trim());
            thisSession.put("header_user-agent", conn.getRemoteIdentification().trim());
            ServerSessionSSH.sessionLookup.put(conn.getSessionId(), thisSession);
            p.put("session", thisSession);
            p.put("session_id", conn.getSessionId());
            thisSession.uiPUT("dont_read", "true");
            thisSession.uiPUT("dont_write", "true");
            thisSession.uiPUT("ssh_remote_port", String.valueOf(conn.getRemotePort()));
            ServerStatus.thisObj.hold_user_pointer(thisSession.user_info);
            thisSession.add_log("[" + server_item.getProperty("serverType", "ftp") + ":" + server_item.getProperty("ip", "0.0.0.0") + ":" + server_item.getProperty("port", "21") + "][" + thisSession.uiSG("user_number") + "] " + LOC.G("Accepting connection from") + ": " + thisSession.uiSG("user_ip") + ":" + conn.getRemotePort() + "\r\n", "SSH_SESSION_ACCEPT");
            if (ServerStatus.BG("block_hack_username_immediately")) {
                if (SessionCrush.isHackUsername(username, ServerStatus.SG("hack_usernames"))) {
                    Log.log("SERVER", 0, "User " + username + " kicked immediately because they are in the hack usernames list. IP: " + thisSession.uiSG("user_ip"));
                    thisSession.uiPUT("hack_username", "true");
                    ServerStatus.thisObj.ban(thisSession.user_info, ServerStatus.IG("hban_timeout"), "hack username:" + username);
                    ServerStatus.thisObj.kick(thisSession.user_info);
                }
            }
        }
        thisSession.uiPUT("user_name", username);
        return thisSession;
    }

    public boolean logonUser(Connection conn, String username, SocketAddress ipAddress) {
        return true;
    }

    public static void endSession(final SessionCrush thisSession) {
        try {
            Thread.sleep(Integer.parseInt(System.getProperty("crushftp.sftp.logout.delay", "100")));
            if (thisSession != null) {
                Log.log("SSH_SERVER", 2, "SSH PORT CONNECTOR logoff RELEASE:" + thisSession.user_info.getProperty("ssh_remote_port", "0"));
                ServerSessionSSH.connectionLookup.remove(thisSession.user_info.getProperty("ssh_remote_port", "0"));
                ServerStatus.thisObj.remove_user(thisSession.user_info, false);
                thisSession.session_socks.removeAllElements();
                Worker.startWorker(new Runnable(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     */
                    @Override
                    public void run() {
                        Object object = thisSession.close_session_sync;
                        synchronized (object) {
                            thisSession.do_kill(null);
                        }
                    }
                });
            }
        }
        catch (Exception e) {
            Log.log("SSH_SERVER", 0, e);
        }
    }

    public String getUserGroup(Connection conn, String username) {
        return "users";
    }

    public String getHomeDirectory(Connection conn) {
        return ".";
    }

    public String getGroup(Connection conn) {
        return "group";
    }

    public void startSession(Connection conn) {
        SessionCrush thisSession = SSHCrushAuthentication8.getSession(conn.getSessionId());
        if (!thisSession.uiBG("user_logged_in")) {
            thisSession.uiBG("publickey_auth_ok");
        }
    }
}

