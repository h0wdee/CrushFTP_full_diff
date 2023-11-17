/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.SFTPClient;
import com.maverick.ssh.PasswordAuthentication;
import com.maverick.ssh.SshConnector;
import com.maverick.ssh2.Ssh2Client;
import com.sshtools.net.ForwardingClient;
import com.sshtools.net.SocketWrapper;
import crushftp.server.daemon.DMZServerCommon;
import crushftp.server.daemon.DMZTunnelClient;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class DMZTunnelClientSSH
extends DMZTunnelClient {
    String ssh_server_ip = "0.0.0.0";
    int ssh_server_port = 0;
    int dmz_port = 0;
    int dmz_dest_port = 0;
    String ssh_username = "";
    String ssh_password = "";
    Ssh2Client ssh = null;
    ForwardingClient fc = null;
    static boolean initialized = false;

    public DMZTunnelClientSSH(String dest_server_ip, int dest_server_port, String name, byte[] tunnel_id2, StringBuffer die_now, DMZServerCommon dmz) {
        super(dest_server_ip, dest_server_port, name, tunnel_id2, die_now, dmz);
        this.ssh_server_ip = dest_server_ip;
        this.ssh_server_port = dest_server_port;
        try {
            ServerSocket ss = new ServerSocket(0);
            this.dmz_port = ss.getLocalPort();
            ss.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        this.dmz_dest_port = dest_server_port + 1;
        this.ssh_username = "crushftp";
        this.ssh_password = "crushftp";
    }

    @Override
    public void go() throws Exception {
        if (!initialized) {
            new SFTPClient("", "", null).close();
        }
        initialized = true;
        SshConnector con = SshConnector.createInstance();
        this.ssh = (Ssh2Client)con.connect(new SocketWrapper(new Socket(this.ssh_server_ip, this.ssh_server_port)), this.ssh_username);
        PasswordAuthentication pass_obj = new PasswordAuthentication();
        pass_obj.setPassword(this.ssh_password);
        this.ssh.authenticate(pass_obj);
        this.fc = new ForwardingClient(this.ssh);
        this.fc.startLocalForwarding("127.0.0.1", this.dmz_port, "127.0.0.1", this.dmz_dest_port);
    }

    @Override
    public int getLocalPort() {
        return this.dmz_port;
    }

    @Override
    public void close() {
        try {
            this.fc.exit();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.ssh.disconnect();
        }
        catch (Exception exception) {
            // empty catch block
        }
    }
}

