/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.nio.Daemon
 *  com.maverick.nio.DaemonContext
 *  com.maverick.nio.ProtocolContext
 *  com.maverick.sshd.AuthenticationMechanismFactory
 *  com.maverick.sshd.Authenticator
 *  com.maverick.sshd.Connection
 *  com.maverick.sshd.PasswordAuthenticationProvider
 *  com.maverick.sshd.SshContext
 *  com.maverick.sshd.auth.DefaultAuthenticationMechanismFactory
 *  com.maverick.sshd.platform.PasswordChangeException
 */
package crushftp.server.daemon;

import com.crushftp.client.Common;
import com.maverick.events.Event;
import com.maverick.events.EventListener;
import com.maverick.nio.Daemon;
import com.maverick.nio.DaemonContext;
import com.maverick.nio.ProtocolContext;
import com.maverick.sshd.AuthenticationMechanismFactory;
import com.maverick.sshd.Authenticator;
import com.maverick.sshd.Connection;
import com.maverick.sshd.PasswordAuthenticationProvider;
import com.maverick.sshd.SshContext;
import com.maverick.sshd.auth.DefaultAuthenticationMechanismFactory;
import com.maverick.sshd.platform.PasswordChangeException;
import crushftp.handlers.Log;
import crushftp.license.Maverick;
import java.io.File;
import java.io.IOException;
import java.util.Date;

public class DMZTunnelServerSSH
extends Daemon {
    SshContext sshContext = null;
    public static String allowed_ips = "";
    String bind_ip = "0.0.0.0";
    int bind_port = 0;
    DMZTunnelServerSSH thisObj = this;

    public DMZTunnelServerSSH(String bind_ip, int bind_port) {
        this.bind_ip = bind_ip;
        this.bind_port = bind_port;
    }

    protected void configure(DaemonContext context) throws IOException {
        Maverick.initLicense();
        SshContext sshContext = new SshContext((Daemon)this);
        try {
            sshContext.loadOrGenerateHostKey(new File("tunnel_key"), "ssh-rsa", 2048);
            sshContext.setMaximumPacketLength(140000);
            sshContext.setMaxumumSessionWindowSpace(0x400000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        DefaultAuthenticationMechanismFactory authFactory = new DefaultAuthenticationMechanismFactory();
        sshContext.setAuthenicationMechanismFactory((AuthenticationMechanismFactory)authFactory);
        authFactory.addProvider((Authenticator)new PasswordAuthenticationProvider(){

            public boolean changePassword(Connection con, String username, String oldPassword, String newPassword) throws PasswordChangeException {
                return false;
            }

            public boolean verifyPassword(Connection con, String username, String password) throws PasswordChangeException {
                String incoming_ip = con.getRemoteAddress();
                if (allowed_ips.equals("") && !incoming_ip.equals("127.0.0.1")) {
                    allowed_ips = String.valueOf(incoming_ip.substring(0, incoming_ip.lastIndexOf(".") + 1)) + "*";
                }
                if (!allowed_ips.equals("") && !Common.do_search(allowed_ips, incoming_ip, false, 0) && allowed_ips.indexOf(incoming_ip) < 0) {
                    System.out.println("TUNNEL:IP " + incoming_ip + " was from an untrusted host and was denied DMZ server control. Allowed IPs: " + allowed_ips);
                    return false;
                }
                return true;
            }
        });
        this.addShutdownHook(new Runnable(){

            @Override
            public void run() {
                System.out.println(new Date() + ":DMZ Tunnel shutting down!!!");
                if (DMZTunnelServerSSH.this.thisObj.getContext().getServer().getLastError() != null) {
                    DMZTunnelServerSSH.this.thisObj.getContext().getServer().getLastError().printStackTrace();
                    Log.log("SERVER", 0, DMZTunnelServerSSH.this.thisObj.getContext().getServer().getLastError());
                }
                Log.log("SERVER", 0, "DMZ Tunnel shutting down!!!");
            }
        });
        sshContext.addEventListener(new EventListener(){

            @Override
            public void processEvent(Event evt) {
                try {
                    if (evt == null) {
                        return;
                    }
                    Throwable t = null;
                    String s = "";
                    if (evt.getAttribute("CONNECTION") != null) {
                        Connection con = (Connection)evt.getAttribute("CONNECTION");
                        s = String.valueOf(s) + "CONNECTION:" + con.getUsername() + "@" + con.getRemoteAddress() + ":";
                    }
                    if (evt.getAttribute("LOG_MESSAGE") != null) {
                        s = String.valueOf(s) + evt.getAttribute("LOG_MESSAGE").toString();
                    }
                    if (evt.getAttribute("IP") != null) {
                        s = String.valueOf(s) + ":" + evt.getAttribute("IP").toString();
                    }
                    if (evt.getAttribute("THROWABLE") != null) {
                        t = (Throwable)evt.getAttribute("THROWABLE");
                    }
                    if (t != null) {
                        t.printStackTrace();
                        Log.log("SERVER", 0, t);
                    }
                    if (s.equals("")) {
                        return;
                    }
                    s = new Date() + "|DMZTUNNEL:" + s;
                    if (Integer.parseInt(System.getProperty("crushftp.debug", "2")) >= 2) {
                        System.out.println(s);
                    }
                    Log.log("SERVER", 2, s);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        context.addListeningInterface(this.bind_ip, this.bind_port, (ProtocolContext)sshContext);
    }

    public void close() {
        this.shutdownNow(false, 0L);
    }
}

