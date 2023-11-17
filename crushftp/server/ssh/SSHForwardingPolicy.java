/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.maverick.sshd.Connection
 *  com.maverick.sshd.ForwardingPolicy
 */
package crushftp.server.ssh;

import com.maverick.sshd.Connection;
import com.maverick.sshd.ForwardingPolicy;
import crushftp.handlers.SessionCrush;
import crushftp.server.ServerStatus;
import crushftp.server.ssh.SSHCrushAuthentication8;
import java.util.Properties;
import java.util.Vector;

public class SSHForwardingPolicy
extends ForwardingPolicy {
    public boolean checkHostPermitted(Connection arg0, String arg1, int arg2) {
        String ip = arg1;
        if (arg1.equals("localhost")) {
            ip = "127.0.0.1";
        }
        Vector tunnelConfigs = this.getTunnels(arg0.getSessionId(), arg0.getUsername());
        int x = 0;
        while (x < tunnelConfigs.size()) {
            Properties tunnel = (Properties)tunnelConfigs.elementAt(x);
            if (tunnel.getProperty("tunnelType", "").equalsIgnoreCase("SSH")) {
                if (tunnel.getProperty("configurable", "false").equals("true")) {
                    return true;
                }
                if (tunnel.getProperty("destIp", "").equals(ip) && tunnel.getProperty("destPort", "").equals(String.valueOf(arg2))) {
                    return true;
                }
            }
            ++x;
        }
        return false;
    }

    public boolean checkInterfacePermitted(Connection arg0, String arg1, int arg2) {
        String ip = arg1;
        if (arg1.equals("localhost")) {
            ip = "127.0.0.1";
        }
        Vector tunnelConfigs = this.getTunnels(arg0.getSessionId(), arg0.getUsername());
        int x = 0;
        while (x < tunnelConfigs.size()) {
            Properties tunnel = (Properties)tunnelConfigs.elementAt(x);
            if (tunnel.getProperty("tunnelType", "").equalsIgnoreCase("SSH")) {
                if (tunnel.getProperty("configurable", "false").equals("true")) {
                    return true;
                }
                if (tunnel.getProperty("bindIp", "").equals(ip) && tunnel.getProperty("localPort", "").equals(String.valueOf(arg2))) {
                    return true;
                }
            }
            ++x;
        }
        return false;
    }

    private Vector getTunnels(String sessionid, String username) {
        SessionCrush thisSession = SSHCrushAuthentication8.getSession(sessionid);
        Vector<Properties> tunnelConfigs = new Vector<Properties>();
        Vector tunnels = (Vector)ServerStatus.VG("tunnels").clone();
        tunnels.addAll(ServerStatus.VG("tunnels_dmz"));
        int x = 0;
        while (x < tunnels.size()) {
            Properties tunnel = null;
            Properties p = (Properties)tunnels.elementAt(x);
            String[] userTunnels = thisSession.SG("tunnels").split(",");
            int xx = 0;
            while (xx < userTunnels.length) {
                if (p.getProperty("id").equals(userTunnels[xx].trim())) {
                    tunnel = (Properties)p.clone();
                }
                ++xx;
            }
            if (tunnel != null) {
                tunnelConfigs.addElement(tunnel);
            }
            ++x;
        }
        return tunnelConfigs;
    }
}

