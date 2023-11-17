/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.Vector;

public class FTPClientSSLSessionProxy {
    ServerSocket ss = null;
    Vector next_connect = new Vector();
    Vector status = new Vector();
    Properties config = null;
    Socket control_sock1 = null;
    Socket control_sock2 = null;
    String url = "";
    public long last_active = System.currentTimeMillis();

    public FTPClientSSLSessionProxy(String url, Properties config_local) {
        this.config = config_local;
        this.url = url;
        try {
            this.ss = new ServerSocket(0);
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    while (!FTPClientSSLSessionProxy.this.ss.isClosed() && (FTPClientSSLSessionProxy.this.control_sock1 == null || FTPClientSSLSessionProxy.this.control_sock1.isConnected() && !FTPClientSSLSessionProxy.this.control_sock1.isClosed())) {
                        Socket sock1 = null;
                        Socket sock2 = null;
                        try {
                            FTPClientSSLSessionProxy.this.ss.setSoTimeout(10000);
                            sock1 = FTPClientSSLSessionProxy.this.ss.accept();
                            String dest = FTPClientSSLSessionProxy.this.next_connect.remove(0).toString();
                            sock2 = Common.getSocket("FTP", new VRL("ftp://" + dest + "/"), Common.dmz_mode ? "no_dmz_que_or_data_socket" : FTPClientSSLSessionProxy.this.config.getProperty("use_dmz", "false"), "", Integer.parseInt(FTPClientSSLSessionProxy.this.config.getProperty("timeout", "30000")));
                            if (FTPClientSSLSessionProxy.this.control_sock2 == null) {
                                FTPClientSSLSessionProxy.this.control_sock2 = sock2;
                            }
                            FTPClientSSLSessionProxy.this.status.addElement("connected");
                            Common.streamCopier(sock1, sock2, sock1.getInputStream(), sock2.getOutputStream(), true, true, true);
                            Common.streamCopier(sock2, sock1, sock2.getInputStream(), sock1.getOutputStream(), true, true, true);
                        }
                        catch (SocketTimeoutException e) {
                            Common.log("FTP_CLIENT", 2, e);
                        }
                        catch (Exception e) {
                            FTPClientSSLSessionProxy.this.status.addElement(e);
                            try {
                                sock1.close();
                            }
                            catch (Exception e2) {
                                Common.log("FTP_CLIENT", 2, e);
                            }
                            try {
                                sock2.close();
                            }
                            catch (Exception e2) {
                                Common.log("FTP_CLIENT", 2, e);
                            }
                        }
                        if (System.currentTimeMillis() - FTPClientSSLSessionProxy.this.last_active <= Long.parseLong(System.getProperty("crushftp.ftp_client_ssl_proxy_timeout", "30")) * 1000L) continue;
                        Common.log("FTP_CLIENT", 1, "Innactivity timeout for FTP client SSLProxy.");
                        FTPClientSSLSessionProxy.this.close();
                    }
                    try {
                        FTPClientSSLSessionProxy.this.ss.close();
                    }
                    catch (IOException e) {
                        Common.log("FTP_CLIENT", 2, e);
                    }
                }
            }, "FTPClientSSLSessionProxy: Url : " + new VRL(url).safe());
        }
        catch (Exception e) {
            Common.log("FTP_CLIENT", 2, e);
        }
    }

    public Socket getDestControlSocket() {
        return this.control_sock2;
    }

    public synchronized Socket getSock(String host, int port) throws Exception {
        this.next_connect.addElement(String.valueOf(host) + ":" + port);
        this.status = new Vector();
        Socket sock = new Socket(InetAddress.getByName("127.0.0.1"), this.ss.getLocalPort());
        if (this.control_sock1 == null) {
            this.control_sock1 = sock;
        }
        int loops = 0;
        long start = System.currentTimeMillis();
        while (this.status.size() == 0 && System.currentTimeMillis() - start < (long)Integer.parseInt(this.config.getProperty("timeout", "30000"))) {
            try {
                Thread.sleep(loops++);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        if (this.status.size() == 1 && this.status.elementAt(0).toString().equals("connected")) {
            return sock;
        }
        sock.close();
        if (this.status.size() == 0) {
            throw new IOException("FTPClientSSLSessionProxy timeout of " + this.config.getProperty("timeout", "30000") + "ms while trying to connect to " + host + ":" + port);
        }
        throw (Exception)this.status.elementAt(0);
    }

    public void close() {
        try {
            this.ss.close();
            if (this.control_sock1 != null) {
                this.control_sock1.close();
            }
            if (this.control_sock2 != null) {
                this.control_sock2.close();
            }
        }
        catch (IOException e) {
            Common.log("FTP_CLIENT", 2, e);
        }
    }

    public void active() {
        this.last_active = System.currentTimeMillis();
    }
}

