/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.HttpCommandHandler;
import com.crushftp.client.Worker;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;
import java.util.SimpleTimeZone;

public class HTTPD {
    SimpleDateFormat sdf_rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    Properties sessions = new Properties();
    HttpCommandHandler handler = null;
    static ServerSocket ss = null;

    public HTTPD() {
        this.sdf_rfc1123.setCalendar(Calendar.getInstance(new SimpleTimeZone(0, "GMT")));
        try {
            Class<?> c = Class.forName(System.getProperty("com.crushftp.server.httphandler"));
            Constructor<?> cons = c.getConstructor(null);
            this.handler = (HttpCommandHandler)cons.newInstance(null);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.getProperties().put("crushftp.worker.v9", "true");
        Common.trustEverything();
        HttpURLConnection.setFollowRedirects(true);
        HTTPD h = new HTTPD();
        Thread.currentThread().setName("HTTP Server Socket:" + System.getProperty("crushftp.agent.port", "33333"));
        try {
            ss = null;
            if (System.getProperty("com.crushftp.server.httpssl", "false").equals("true")) {
                try {
                    ss = Common.getSSLServerSocket(Integer.parseInt(System.getProperty("crushftp.agent.port", "33333")), System.getProperty("crushftp.agent.bind_ip", "0.0.0.0"), true, System.getProperty("com.crushftp.server.httpssl_keystore", "builtin"), System.getProperty("com.crushftp.server.httpssl_keystore_pass", "crushftp"), System.getProperty("com.crushftp.server.httpssl_keystore_key_pass", "crushftp"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    ss = Common.getSSLServerSocket(Integer.parseInt(System.getProperty("crushftp.agent.port", "33333")), System.getProperty("crushftp.agent.bind_ip", "0.0.0.0"), true, "builtin", "crushftp", "crushftp");
                }
            } else {
                ss = new ServerSocket(Integer.parseInt(System.getProperty("crushftp.agent.port", "33333")), 1000, InetAddress.getByName(System.getProperty("crushftp.agent.bind_ip", "0.0.0.0")));
            }
            while (true) {
                Socket sock = ss.accept();
                h.process(sock);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void shutdown() {
        try {
            ss.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public void process(final Socket sock) {
        Runnable r = new Runnable(){

            @Override
            public void run() {
                InputStream in = null;
                OutputStream out = null;
                try {
                    try {
                        in = sock.getInputStream();
                        out = sock.getOutputStream();
                        sock.setSoTimeout(30000);
                        while (HTTPD.this.processRequest(out, in, sock)) {
                        }
                    }
                    catch (SocketTimeoutException socketTimeoutException) {
                        try {
                            in.close();
                            out.close();
                            sock.close();
                        }
                        catch (IOException iOException) {}
                    }
                    catch (IOException e) {
                        block17: {
                            if (("" + e).indexOf("socket write error") >= 0) break block17;
                            e.printStackTrace();
                        }
                        try {
                            in.close();
                            out.close();
                            sock.close();
                        }
                        catch (IOException iOException) {}
                    }
                }
                finally {
                    try {
                        in.close();
                        out.close();
                        sock.close();
                    }
                    catch (IOException iOException) {}
                }
            }
        };
        try {
            Worker.startWorker(r);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean processRequest(OutputStream out, InputStream in, Socket sock) throws IOException {
        String ip;
        Properties session;
        Properties request = new Properties();
        Properties headers = new Properties();
        String data = "";
        while ((data = HttpCommandHandler.readLine(in)) != null) {
            if (data == null) {
                return false;
            }
            if (headers.size() == 0) {
                headers.put("0", data);
            } else {
                String key = data;
                String val = "";
                if (data.indexOf(":") >= 0) {
                    key = data.substring(0, data.indexOf(":"));
                    val = data.substring(data.indexOf(":") + 1).trim();
                }
                headers.put(key.toLowerCase(), val);
                headers.put(key, val);
            }
            if (data.equals("")) break;
        }
        if (headers.size() == 0) {
            return false;
        }
        String h0 = headers.getProperty("0");
        if (h0.indexOf("?") >= 0) {
            String s = h0.substring(h0.indexOf("?") + 1, h0.lastIndexOf(" "));
            h0 = String.valueOf(h0.substring(0, h0.indexOf("?"))) + " HTTP/1.1";
            HttpCommandHandler.parseParams(s, request);
        }
        if ((session = (Properties)this.sessions.get(sock.getInetAddress().getHostAddress())) == null) {
            session = new Properties();
            this.sessions.put(sock.getInetAddress().getHostAddress(), session);
        }
        if ((ip = request.getProperty("ip")) != null && ip.equals("auto")) {
            ip = null;
        }
        if (ip != null && ip.equals("")) {
            ip = null;
        }
        if (ip == null) {
            ip = sock.getInetAddress().getHostAddress();
        }
        String path = h0.substring(h0.indexOf(" ") + 1, h0.lastIndexOf(" "));
        if ((path = Common.dots(path)).startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/") || path.equals("")) {
            path = String.valueOf(path) + "index.html";
        }
        this.handler.handleCommand(path, headers, request, out, in, session, ip);
        Thread.currentThread().setName("Waiting for HTTP command...");
        return true;
    }
}

