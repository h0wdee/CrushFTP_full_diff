/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel2;

import com.crushftp.tunnel2.ConnectionHandler;
import com.crushftp.tunnel2.Queue;
import com.crushftp.tunnel2.Tunnel2;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class FTPProxy {
    static int pasv_port = 0;
    Tunnel2 t = null;
    Queue q = null;
    Properties ftpPortTweaker = new Properties();

    public FTPProxy(Tunnel2 t, Queue q) {
        this.t = t;
        this.q = q;
    }

    static ServerSocket getNextPasvSocket() {
        int port1 = Integer.parseInt(System.getProperty("crushtunnel.pasv.port.start", "0"));
        int port2 = Integer.parseInt(System.getProperty("crushtunnel.pasv.port.stop", "0"));
        int loops = 0;
        while (loops++ < 1000) {
            try {
                if (port1 >= 0 && port1 < port2) {
                    if (pasv_port < port1) {
                        pasv_port = port1;
                    } else if (pasv_port > port2) {
                        pasv_port = port1;
                    }
                    ++pasv_port;
                }
                return new ServerSocket(pasv_port);
            }
            catch (IOException iOException) {
                try {
                    Thread.sleep(100L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
            }
        }
        return null;
    }

    public void proxyNATs(Socket control, Socket proxy) throws Exception {
        this.proxyNAT(control, proxy);
        this.proxyNAT(proxy, control);
    }

    private Thread proxyNAT(final Socket control, final Socket proxy) throws Exception {
        Thread thread = new Thread(new Runnable(){

            /*
             * Enabled aggressive block sorting
             * Enabled unnecessary exception pruning
             * Enabled aggressive exception aggregation
             */
            @Override
            public void run() {
                Thread.currentThread().setName("Tunnel FTP Proxy:" + control);
                try {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(control.getInputStream(), "UTF8"));
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proxy.getOutputStream(), "UTF8"));
                        String line = "";
                        while (true) {
                            int activePort;
                            line = br.readLine();
                            if (line == null) return;
                            if (FTPProxy.this.q.isClosedLocal()) {
                                return;
                            }
                            if (System.getProperty("tunnel.debug.ftp", "false").equals("true")) {
                                Tunnel2.msg("IN:" + control + ":" + line);
                            }
                            if (line.toUpperCase().startsWith("LIST ") || line.toUpperCase().startsWith("STOR ") || line.toUpperCase().startsWith("APPE ") || line.toUpperCase().startsWith("RETR ") || line.startsWith("150 ") || line.startsWith("125 ")) {
                                control.setSoTimeout(0);
                                proxy.setSoTimeout(0);
                            } else {
                                control.setSoTimeout(3600000);
                                proxy.setSoTimeout(3600000);
                            }
                            if (line.startsWith("227 ")) {
                                String pasvIpTmp = line.substring(line.lastIndexOf("(") + 1, line.lastIndexOf(")"));
                                final int pasvPort = Integer.parseInt(pasvIpTmp.split(",")[4].trim()) * 256 + Integer.parseInt(pasvIpTmp.split(",")[5].trim());
                                pasvIpTmp = pasvIpTmp.substring(0, pasvIpTmp.lastIndexOf(","));
                                pasvIpTmp = pasvIpTmp.substring(0, pasvIpTmp.lastIndexOf(","));
                                final String pasvIp = pasvIpTmp = pasvIpTmp.replace(',', '.').trim();
                                final ServerSocket ssProxyData = FTPProxy.getNextPasvSocket();
                                line = "227 Entering Passive Mode (" + System.getProperty("crushtunnel.pasv.ip", "127.0.0.1").replace('.', ',') + "," + ssProxyData.getLocalPort() / 256 + "," + (ssProxyData.getLocalPort() - ssProxyData.getLocalPort() / 256 * 256) + ")";
                                final ConnectionHandler ch = new ConnectionHandler(FTPProxy.this.t);
                                if (FTPProxy.this.ftpPortTweaker.getProperty("activePassiveSwap", "").equals("true")) {
                                    FTPProxy.this.ftpPortTweaker.put("activePassiveSwap", "false");
                                    line = "200 PORT command successful.";
                                    Socket sock1 = new Socket(FTPProxy.this.ftpPortTweaker.getProperty("lastActiveIp"), Integer.parseInt(FTPProxy.this.ftpPortTweaker.getProperty("lastActivePort")));
                                    ch.process(sock1, pasvIp, pasvPort, false, true, 0);
                                    ssProxyData.close();
                                } else {
                                    new Thread(new Runnable(){

                                        @Override
                                        public void run() {
                                            Thread.currentThread().setName("Tunnel FTP Proxy PASV:" + control + " pasvIp:" + pasvIp + " pasvPort:" + pasvPort);
                                            try {
                                                ch.process(ssProxyData.accept(), pasvIp, pasvPort, false, true, 0);
                                            }
                                            catch (Exception e) {
                                                Tunnel2.msg(e);
                                            }
                                            try {
                                                ssProxyData.close();
                                            }
                                            catch (Exception exception) {
                                                // empty catch block
                                            }
                                        }
                                    }).start();
                                }
                            } else if (line.startsWith("229 ")) {
                                line = line.substring(line.lastIndexOf("(") + 1, line.lastIndexOf(")"));
                                final int pasvPort = Integer.parseInt(line.split("\\|")[3]);
                                final ServerSocket ssProxyData = FTPProxy.getNextPasvSocket();
                                final ConnectionHandler ch = new ConnectionHandler(FTPProxy.this.t);
                                line = "229 Entering Extended Passive Mode (|||" + ssProxyData.getLocalPort() + "|)";
                                new Thread(new Runnable(){

                                    @Override
                                    public void run() {
                                        Thread.currentThread().setName("Tunnel FTP Proxy PASV:" + control + " pasvIp:" + (this).FTPProxy.this.t.tunnel.getProperty("destIp") + " pasvPort:" + pasvPort);
                                        try {
                                            ch.process(ssProxyData.accept(), (this).FTPProxy.this.t.tunnel.getProperty("destIp"), pasvPort, false, true, 0);
                                        }
                                        catch (Exception e) {
                                            Tunnel2.msg(e);
                                        }
                                        try {
                                            ssProxyData.close();
                                        }
                                        catch (Exception exception) {
                                            // empty catch block
                                        }
                                    }
                                }).start();
                            } else if (line.startsWith("PORT ")) {
                                String activeIp = line.substring(line.indexOf(" ") + 1).trim();
                                activePort = Integer.parseInt(activeIp.split(",")[4].trim()) * 256 + Integer.parseInt(activeIp.split(",")[5].trim());
                                activeIp = activeIp.substring(0, activeIp.lastIndexOf(","));
                                activeIp = activeIp.substring(0, activeIp.lastIndexOf(","));
                                activeIp = activeIp.replace(',', '.').trim();
                                FTPProxy.this.ftpPortTweaker.put("activePassiveSwap", "true");
                                FTPProxy.this.ftpPortTweaker.put("lastActiveIp", activeIp);
                                FTPProxy.this.ftpPortTweaker.put("lastActivePort", String.valueOf(activePort));
                                line = "PASV";
                            } else if (line.startsWith("EPRT ")) {
                                String activeIp = line.split("\\|")[2].trim();
                                activePort = Integer.parseInt(line.split("\\|")[3].trim());
                                FTPProxy.this.ftpPortTweaker.put("activePassiveSwap", "true");
                                FTPProxy.this.ftpPortTweaker.put("lastActiveIp", activeIp);
                                FTPProxy.this.ftpPortTweaker.put("lastActivePort", String.valueOf(activePort));
                                line = "PASV";
                            }
                            if (System.getProperty("tunnel.debug.ftp", "false").equals("true")) {
                                Tunnel2.msg("OUT:" + control + ":" + line);
                            }
                            bw.write(String.valueOf(line) + "\r\n");
                            bw.flush();
                        }
                    }
                    catch (Exception e) {
                        Tunnel2.msg(e);
                        try {
                            control.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        try {
                            proxy.close();
                            return;
                        }
                        catch (Exception exception) {
                            return;
                        }
                    }
                }
                finally {
                    try {
                        control.close();
                    }
                    catch (Exception exception) {}
                    try {
                        proxy.close();
                    }
                    catch (Exception exception) {}
                }
            }
        });
        thread.start();
        return thread;
    }
}

