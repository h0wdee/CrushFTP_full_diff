/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel2;

import com.crushftp.client.Common;
import com.crushftp.tunnel2.Chunk;
import com.crushftp.tunnel2.DProperties;
import com.crushftp.tunnel2.FTPProxy;
import com.crushftp.tunnel2.Queue;
import com.crushftp.tunnel2.Tunnel2;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Vector;

public class ConnectionHandler
implements Runnable {
    Tunnel2 t = null;
    String bindip = null;
    int bindport = 0;
    Vector threads = new Vector();
    static long lastUpdateSend = System.currentTimeMillis();
    static long lastUpdateReceive = System.currentTimeMillis();

    public ConnectionHandler(Tunnel2 t, String bindip, int bindport) {
        this.t = t;
        this.bindip = bindip;
        this.bindport = bindport;
        if (t.tunnel.getProperty("validate_cert", "false").equals("false")) {
            Common.trustEverything();
        }
        System.getProperties().put("sun.net.http.retryPost", "false");
    }

    public ConnectionHandler(Tunnel2 t) {
        this.t = t;
    }

    @Override
    public void run() {
        block60: {
            ServerSocket ss1 = null;
            ServerSocket ss2 = null;
            Thread.currentThread().setName("Tunnel:ConnectionHandler:bindip:" + this.bindip + ":" + this.bindport);
            try {
                try {
                    if (this.t.tunnel.getProperty("reverse", "false").equals("true") && !this.t.allowReverseMode) {
                        this.t.startStopTunnel(true);
                    }
                    ServerSocket ss0 = null;
                    int sock_num = 0;
                    while (this.t.isActive()) {
                        if (this.t.tunnel.getProperty("reverse", "false").equals("false") || this.t.allowReverseMode) {
                            try {
                                if (ss1 == null) {
                                    ss1 = new ServerSocket(this.bindport, 1000, InetAddress.getByName(this.bindip));
                                    Tunnel2.msg("Tunnel2:ConnectionHandler:bound port:" + this.bindport);
                                    ss1.setSoTimeout(100);
                                    if (!this.t.allowReverseMode) {
                                        this.t.startStopTunnel(true);
                                    }
                                    Tunnel2.msg("Tunnel2:ConnectionHandler:tunnel started.");
                                }
                                if (String.valueOf(this.bindport).startsWith("444") && ss2 == null) {
                                    ss2 = new ServerSocket(this.bindport + 10, 1000, InetAddress.getByName(this.bindip));
                                    Tunnel2.msg("Tunnel2:ConnectionHandler:bound port:" + (this.bindport + 10));
                                    ss2.setSoTimeout(100);
                                }
                                this.t.markAvailable();
                                if (++sock_num == 1) {
                                    ss0 = ss1;
                                } else {
                                    ss0 = ss2;
                                    sock_num = 0;
                                }
                                if (ss0 == null) {
                                    ss0 = ss1;
                                }
                                Socket proxy = ss0.accept();
                                Tunnel2.msg("Tunnel2:ConnectionHandler:received connection:" + proxy);
                                Socket control = proxy;
                                boolean ftp = String.valueOf(this.bindport).endsWith("21");
                                try {
                                    if (String.valueOf(this.bindport).startsWith("444")) {
                                        if (ss0.getLocalPort() == ss2.getLocalPort()) {
                                            ftp = true;
                                        }
                                        if (ftp) {
                                            this.t.tunnel.put("destPort", "55521");
                                        } else {
                                            this.t.tunnel.put("destPort", "55580");
                                        }
                                    } else if (this.bindport == 55555 || this.t.tunnel.getProperty("destPort").equals("55555") || this.t.tunnel.getProperty("destPort").equals("55580") || this.t.tunnel.getProperty("destPort").equals("55521") || this.t.tunnel.getProperty("destPort").equals("0")) {
                                        ftp = true;
                                        int x = 0;
                                        while (x < 50 && ftp) {
                                            if (proxy.getInputStream().available() > 0) {
                                                ftp = false;
                                            }
                                            if (ftp) {
                                                Thread.sleep(10L);
                                            }
                                            ++x;
                                        }
                                        if (ftp) {
                                            this.t.tunnel.put("destPort", "55521");
                                        } else {
                                            this.t.tunnel.put("destPort", "55580");
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    Tunnel2.msg(e);
                                    try {
                                        proxy.close();
                                    }
                                    catch (Exception ee) {
                                        Common.log("TUNNEL", 1, ee);
                                    }
                                    if (this.t.onlyOnce) break;
                                }
                                Tunnel2.msg("Tunnel2:ConnectionHandler:ftp=" + ftp);
                                if (ftp) {
                                    ServerSocket ssProxyControl = new ServerSocket(0);
                                    int localPort = ssProxyControl.getLocalPort();
                                    control = new Socket("127.0.0.1", localPort);
                                    Queue q = this.process(ssProxyControl.accept(), this.t.tunnel.getProperty("destIp"), Integer.parseInt(this.t.tunnel.getProperty("destPort")), false, true, 0);
                                    ssProxyControl.close();
                                    FTPProxy ftpp = new FTPProxy(this.t, q);
                                    ftpp.proxyNATs(control, proxy);
                                    Tunnel2.msg("Tunnel2:Started FTP NAT:control=" + control + " proxy=" + proxy);
                                } else {
                                    this.process(control, this.t.tunnel.getProperty("destIp"), Integer.parseInt(this.t.tunnel.getProperty("destPort")), false, true, 0);
                                }
                            }
                            catch (SocketTimeoutException proxy) {
                            }
                            catch (Exception e) {
                                Tunnel2.msg(e);
                                try {
                                    Thread.sleep(1000L);
                                }
                                catch (Exception ee) {
                                    Common.log("TUNNEL", 1, ee);
                                }
                            }
                            if (this.t.allowReverseMode && System.currentTimeMillis() - this.t.lastActivity > 30000L) {
                                Tunnel2.msg("Tunnel is apparently inactive..." + (System.currentTimeMillis() - this.t.lastActivity));
                                this.t.stopThisTunnel();
                            }
                        } else {
                            this.t.markAvailable();
                            Thread.sleep(1000L);
                        }
                        if (this.t.onlyOnce) break;
                    }
                    if (ss1 != null) {
                        ss1.close();
                    }
                    if (ss2 != null) {
                        ss2.close();
                    }
                    this.t.waitForShutdown();
                }
                catch (Exception e) {
                    Common.log("TUNNEL", 1, e);
                    try {
                        if (ss1 != null) {
                            ss1.close();
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    try {
                        if (ss2 != null) {
                            ss2.close();
                        }
                        break block60;
                    }
                    catch (Exception exception) {}
                    break block60;
                }
            }
            catch (Throwable throwable) {
                try {
                    if (ss1 != null) {
                        ss1.close();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    if (ss2 != null) {
                        ss2.close();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                throw throwable;
            }
            try {
                if (ss1 != null) {
                    ss1.close();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                if (ss2 != null) {
                    ss2.close();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        try {
            this.t.startStopTunnel(false);
        }
        catch (Exception e) {
            Common.log("TUNNEL", 1, e);
        }
    }

    public Queue process(final Socket sock, String host, int port, boolean startLocal, boolean startRemote, int qid2) throws Exception {
        if (qid2 == 0) {
            qid2 = (int)(Math.random() * 1.0E9);
        }
        final int qid = qid2;
        if (startRemote) {
            this.t.doConnect(qid, host, port);
        }
        final Queue q = new Queue(this.t, qid);
        this.t.addQueue(q);
        new Thread(new Runnable(){

            @Override
            public void run() {
                block21: {
                    ConnectionHandler.this.threads.addElement(Thread.currentThread());
                    Thread.currentThread().setName("Tunnel:qid=" + qid + ":socket read=" + sock);
                    Tunnel2.msg("Tunnel2:process:read:qid=" + qid + " :socket read=" + sock);
                    BufferedInputStream in = null;
                    try {
                        try {
                            sock.setSoTimeout(10000);
                            in = new BufferedInputStream(sock.getInputStream());
                            int bytesRead = 0;
                            int num = 1;
                            while (bytesRead >= 0 && !q.isClosedLocal()) {
                                byte[] b = DProperties.getArray();
                                try {
                                    bytesRead = in.read(b, 0, 65500);
                                    if (bytesRead > 0) {
                                        Chunk c = new Chunk(qid, b, bytesRead, num++);
                                        q.writeLocal(c, -1);
                                        ConnectionHandler.this.t.addBytesOut(bytesRead);
                                        Tunnel2.writeAck(c, q, ConnectionHandler.this.t);
                                    }
                                }
                                catch (SocketTimeoutException socketTimeoutException) {
                                    // empty catch block
                                }
                                if (System.currentTimeMillis() - lastUpdateSend > 10000L) {
                                    lastUpdateSend = System.currentTimeMillis();
                                    Tunnel2.msg("Tunnel out stats: remoteNum:" + q.remoteNum + " remoteSize:" + q.remote.size() + " localSize:" + q.localNum + " lastNum:" + num + " max:" + q.max + " localBytes:" + ConnectionHandler.this.t.getLocal().getBytes() + " waitingAcks:" + ConnectionHandler.this.t.getWaitingAckCount() + " qid:" + qid + " Free JVM Memory:" + Common.format_bytes_short(Common.getFreeRam()));
                                }
                                while (ConnectionHandler.this.t.getWaitingAckCount() > 100 || ConnectionHandler.this.t.getLocal().getBytes() > (long)(0x100000 * Integer.parseInt(ConnectionHandler.this.t.tunnel.getProperty("sendBuffer", "1")))) {
                                    Thread.sleep(1L);
                                }
                            }
                        }
                        catch (Exception e) {
                            if (!q.isClosedRemote()) {
                                Tunnel2.msg(e);
                            }
                            try {
                                q.closeLocal();
                                Tunnel2.msg("Closing queue:" + qid + " end:" + q.localNum);
                                if (in != null) {
                                    in.close();
                                }
                                q.waitForClose(30);
                            }
                            catch (Exception e2) {
                                Common.log("TUNNEL", 1, e2);
                                q.remote.close();
                            }
                            break block21;
                        }
                    }
                    catch (Throwable throwable) {
                        try {
                            q.closeLocal();
                            Tunnel2.msg("Closing queue:" + qid + " end:" + q.localNum);
                            if (in != null) {
                                in.close();
                            }
                            q.waitForClose(30);
                        }
                        catch (Exception e) {
                            Common.log("TUNNEL", 1, e);
                            q.remote.close();
                        }
                        throw throwable;
                    }
                    try {
                        q.closeLocal();
                        Tunnel2.msg("Closing queue:" + qid + " end:" + q.localNum);
                        if (in != null) {
                            in.close();
                        }
                        q.waitForClose(30);
                    }
                    catch (Exception e) {
                        Common.log("TUNNEL", 1, e);
                        q.remote.close();
                    }
                }
                ConnectionHandler.this.threads.remove(Thread.currentThread());
                ConnectionHandler.this.t.removeQueue(qid);
            }
        }).start();
        new Thread(new Runnable(){

            /*
             * Unable to fully structure code
             */
            @Override
            public void run() {
                block24: {
                    block23: {
                        ConnectionHandler.this.threads.addElement(Thread.currentThread());
                        Thread.currentThread().setName("Tunnel:qid=" + qid + ":socket write=" + sock);
                        Tunnel2.msg("Tunnel2:process:write:qid=" + qid + ":socket write=" + sock);
                        out = null;
                        try {
                            try {
                                out = sock.getOutputStream();
                                num = 0;
                                while (!(q.isClosedRemote() || q.max >= 0 && num >= q.max)) {
                                    c = q.readRemote();
                                    if (c == null) {
                                        Thread.sleep(100L);
                                    } else if (!c.isCommand()) {
                                        num = c.num;
                                        out.write(c.b);
                                        ConnectionHandler.this.t.addBytesIn(c.b.length);
                                        c.b = DProperties.releaseArray(c.b);
                                    }
                                    if (System.currentTimeMillis() - ConnectionHandler.lastUpdateReceive <= 10000L) continue;
                                    ConnectionHandler.lastUpdateReceive = System.currentTimeMillis();
                                    Tunnel2.msg("Tunnel in stats: remoteNum:" + q.remoteNum + " remoteSize:" + q.remote.size() + " localSize:" + q.localNum + " lastNum:" + num + " max:" + q.max + " localBytes:" + ConnectionHandler.this.t.getLocal().getBytes() + " waitingAcks:" + ConnectionHandler.this.t.getWaitingAckCount() + " qid:" + qid + " Free JVM Memory:" + Common.format_bytes_short(Common.getFreeRam()));
                                }
                                sock.close();
                                break block23;
                            }
                            catch (Exception e) {
                                Tunnel2.msg(e);
                                try {
                                    q.closeRemote();
                                    out.close();
                                }
                                catch (Exception e) {
                                    Common.log("TUNNEL", 1, e);
                                }
                                keys = ConnectionHandler.this.t.localAck.keys();
                                ** while (keys.hasMoreElements())
                            }
                        }
                        catch (Throwable var4_20) {
                            try {
                                q.closeRemote();
                                out.close();
                            }
                            catch (Exception e) {
                                Common.log("TUNNEL", 1, e);
                            }
                            keys = ConnectionHandler.this.t.localAck.keys();
                            ** while (keys.hasMoreElements())
                        }
lbl-1000:
                        // 1 sources

                        {
                            key = keys.nextElement().toString();
                            try {
                                c = ConnectionHandler.this.t.localAck.get(key);
                                if (c == null || c.id != q.id) continue;
                                ConnectionHandler.this.t.localAck.remove(key);
                                c.b = DProperties.releaseArray(c.b);
                            }
                            catch (IOException e) {
                                Tunnel2.msg(e);
                            }
                            continue;
lbl47:
                            // 1 sources

                            break block24;
                        }
lbl-1000:
                        // 1 sources

                        {
                            key = keys.nextElement().toString();
                            try {
                                c = ConnectionHandler.this.t.localAck.get(key);
                                if (c == null || c.id != q.id) continue;
                                ConnectionHandler.this.t.localAck.remove(key);
                                c.b = DProperties.releaseArray(c.b);
                            }
                            catch (IOException e) {
                                Tunnel2.msg(e);
                            }
                            continue;
                        }
lbl69:
                        // 1 sources

                        throw var4_20;
                    }
                    try {
                        q.closeRemote();
                        out.close();
                    }
                    catch (Exception e) {
                        Common.log("TUNNEL", 1, e);
                    }
                    keys = ConnectionHandler.this.t.localAck.keys();
                    while (keys.hasMoreElements()) {
                        key = keys.nextElement().toString();
                        try {
                            c = ConnectionHandler.this.t.localAck.get(key);
                            if (c == null || c.id != q.id) continue;
                            ConnectionHandler.this.t.localAck.remove(key);
                            c.b = DProperties.releaseArray(c.b);
                        }
                        catch (IOException e) {
                            Tunnel2.msg(e);
                        }
                    }
                }
                ConnectionHandler.this.threads.remove(Thread.currentThread());
            }
        }).start();
        return q;
    }
}

