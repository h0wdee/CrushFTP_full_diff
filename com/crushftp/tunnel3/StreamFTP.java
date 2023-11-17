/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel3;

import com.crushftp.client.Worker;
import com.crushftp.tunnel3.StreamController;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

public class StreamFTP {
    static int pasv_port = 0;
    StreamController sc = null;
    Properties ftpPortTweaker = new Properties();
    int stream_id = -1;

    public StreamFTP(StreamController sc, int stream_id) {
        this.sc = sc;
        this.stream_id = stream_id;
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

    private void proxyNAT(final Socket control, final Socket proxy) throws Exception {
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                boolean expecting_quit = false;
                Thread.currentThread().setName("Tunnel FTP Proxy:" + control);
                try {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(control.getInputStream(), "UTF8"));
                        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(proxy.getOutputStream(), "UTF8"));
                        String line = "";
                        while ((line = br.readLine()) != null) {
                            int activePort;
                            if (line.toUpperCase().startsWith("221 ") || line.toUpperCase().startsWith("QUIT")) {
                                expecting_quit = true;
                            }
                            if (System.getProperty("tunnel.debug.ftp", "false").equals("true")) {
                                StreamFTP.this.sc.msg("IN:" + control + ":" + line);
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
                                final ServerSocket ssProxyData = StreamFTP.getNextPasvSocket();
                                line = "227 Entering Passive Mode (" + System.getProperty("crushtunnel.pasv.ip", "127.0.0.1").replace('.', ',') + "," + ssProxyData.getLocalPort() / 256 + "," + (ssProxyData.getLocalPort() - ssProxyData.getLocalPort() / 256 * 256) + ")";
                                if (StreamFTP.this.ftpPortTweaker.getProperty("activePassiveSwap", "").equals("true")) {
                                    StreamFTP.this.ftpPortTweaker.put("activePassiveSwap", "false");
                                    line = "200 PORT command successful.";
                                    Socket sock1 = new Socket(StreamFTP.this.ftpPortTweaker.getProperty("lastActiveIp"), Integer.parseInt(StreamFTP.this.ftpPortTweaker.getProperty("lastActivePort")));
                                    StreamFTP.this.sc.process(sock1, pasvIp, pasvPort, 0, StreamFTP.this.stream_id);
                                    ssProxyData.close();
                                } else {
                                    Worker.startWorker(new Runnable(){

                                        @Override
                                        public void run() {
                                            Thread.currentThread().setName("Tunnel FTP Proxy PASV:" + control + " pasvIp:" + pasvIp + " pasvPort:" + pasvPort);
                                            try {
                                                (this).StreamFTP.this.sc.process(ssProxyData.accept(), pasvIp, pasvPort, 0, (this).StreamFTP.this.stream_id);
                                            }
                                            catch (Exception e) {
                                                (this).StreamFTP.this.sc.msg(e);
                                            }
                                            try {
                                                ssProxyData.close();
                                            }
                                            catch (Exception exception) {
                                                // empty catch block
                                            }
                                        }
                                    });
                                }
                            } else if (line.startsWith("229 ")) {
                                line = line.substring(line.lastIndexOf("(") + 1, line.lastIndexOf(")"));
                                final int pasvPort = Integer.parseInt(line.split("\\|")[3]);
                                final ServerSocket ssProxyData = StreamFTP.getNextPasvSocket();
                                line = "229 Entering Extended Passive Mode (|||" + ssProxyData.getLocalPort() + "|)";
                                Worker.startWorker(new Runnable(){

                                    @Override
                                    public void run() {
                                        Thread.currentThread().setName("Tunnel FTP Proxy PASV:" + control + " pasvIp:" + (this).StreamFTP.this.sc.tunnel.getProperty("destIp") + " pasvPort:" + pasvPort);
                                        try {
                                            (this).StreamFTP.this.sc.process(ssProxyData.accept(), (this).StreamFTP.this.sc.tunnel.getProperty("destIp"), pasvPort, 0, (this).StreamFTP.this.stream_id);
                                        }
                                        catch (Exception e) {
                                            (this).StreamFTP.this.sc.msg(e);
                                        }
                                        try {
                                            ssProxyData.close();
                                        }
                                        catch (Exception exception) {
                                            // empty catch block
                                        }
                                    }
                                });
                            } else if (line.startsWith("PORT ")) {
                                String activeIp = line.substring(line.indexOf(" ") + 1).trim();
                                activePort = Integer.parseInt(activeIp.split(",")[4].trim()) * 256 + Integer.parseInt(activeIp.split(",")[5].trim());
                                activeIp = activeIp.substring(0, activeIp.lastIndexOf(","));
                                activeIp = activeIp.substring(0, activeIp.lastIndexOf(","));
                                activeIp = activeIp.replace(',', '.').trim();
                                StreamFTP.this.ftpPortTweaker.put("activePassiveSwap", "true");
                                StreamFTP.this.ftpPortTweaker.put("lastActiveIp", activeIp);
                                StreamFTP.this.ftpPortTweaker.put("lastActivePort", String.valueOf(activePort));
                                line = "PASV";
                            } else if (line.startsWith("EPRT ")) {
                                String activeIp = line.split("\\|")[2].trim();
                                activePort = Integer.parseInt(line.split("\\|")[3].trim());
                                StreamFTP.this.ftpPortTweaker.put("activePassiveSwap", "true");
                                StreamFTP.this.ftpPortTweaker.put("lastActiveIp", activeIp);
                                StreamFTP.this.ftpPortTweaker.put("lastActivePort", String.valueOf(activePort));
                                line = "PASV";
                            }
                            if (System.getProperty("tunnel.debug.ftp", "false").equals("true")) {
                                StreamFTP.this.sc.msg("OUT:" + control + ":" + line);
                            }
                            bw.write(String.valueOf(line) + "\r\n");
                            bw.flush();
                        }
                    }
                    catch (Exception e) {
                        if (!expecting_quit) {
                            StreamFTP.this.sc.msg(e);
                        }
                        try {
                            control.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        try {
                            proxy.close();
                        }
                        catch (Exception exception) {}
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
    }
}

