/*
 * Decompiled with CFR 0.152.
 */
package crushftp.server.daemon;

import com.crushftp.client.Common;
import crushftp.handlers.Log;
import crushftp.server.daemon.DMZServerCommon;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class DMZServer1
extends DMZServerCommon {
    public DMZServer1(Properties server_item) {
        super(server_item);
    }

    @Override
    public void start_connection() throws Exception {
        while (this.die_now.length() == 0) {
            try {
                if (this.read_sock != null) {
                    Common.sockLog(this.read_sock, "read_sock close at port restart");
                    DMZServer1.closeInOutSockRef(this.socks_in_out, this.read_sock);
                }
                if (this.write_sock != null) {
                    Common.sockLog(this.write_sock, "write_sock close at port restart");
                    DMZServer1.closeInOutSockRef(this.socks_in_out, this.write_sock);
                }
                String msg = "Creating read/write sock to DMZ, ssl=" + System.getProperty("crushftp.dmz.ssl", "true");
                System.out.println(new Date() + "|" + msg);
                Log.log("DMZ", 0, msg);
                this.getNewReadSock();
                this.getNewWriteSock();
                break;
            }
            catch (Exception e) {
                Log.log("DMZ", 0, e);
                this.busyMessage = "ERROR connecting to " + this.listen_ip + ":" + this.listen_port + " " + e;
                Log.log("DMZ", 0, this.busyMessage);
                Thread.sleep(1000L);
            }
        }
    }

    @Override
    public void getNewWriteSock() throws Exception {
        if (System.getProperty("crushftp.dmz.ssl", "true").equals("true")) {
            this.write_sock = ((SSLSocketFactory)Common.System2.get("crushftp.dmz.factory")).createSocket(this.listen_ip, this.listen_port);
            crushftp.handlers.Common.configureSSLTLSSocket((SSLSocket)this.write_sock);
        } else {
            this.write_sock = new Socket(this.listen_ip, this.listen_port);
        }
        Common.sockLog(this.write_sock, "write_sock create");
        this.write_sock.setSoTimeout(10000);
        this.write_sock.getOutputStream().write("R".getBytes());
        this.write_sock.getOutputStream().flush();
        DMZServer1.createInOutSockRef(this.socks_in_out, this.write_sock);
    }

    @Override
    public void getNewReadSock() throws Exception {
        if (System.getProperty("crushftp.dmz.ssl", "true").equals("true")) {
            this.read_sock = ((SSLSocketFactory)Common.System2.get("crushftp.dmz.factory")).createSocket(this.listen_ip, this.listen_port);
            crushftp.handlers.Common.configureSSLTLSSocket((SSLSocket)this.read_sock);
        } else {
            this.read_sock = new Socket(this.listen_ip, this.listen_port);
        }
        Common.sockLog(this.read_sock, "read_sock create");
        this.read_sock.setSoTimeout(10000);
        this.read_sock.getOutputStream().write("W".getBytes());
        this.read_sock.getOutputStream().flush();
        DMZServer1.createInOutSockRef(this.socks_in_out, this.read_sock);
    }
}

