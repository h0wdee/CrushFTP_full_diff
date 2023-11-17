/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Date;
import java.util.Vector;

public class BCProxy
implements Runnable {
    public static BCProxy thisObj = null;
    String[] args = null;
    ServerSocket ss = null;
    URL[] urls = new URL[0];
    Thread thread = null;
    public ClassLoader loader = null;

    private BCProxy(Thread thread) {
        this.thread = thread;
        try {
            this.ss = new ServerSocket(0, 100, InetAddress.getByName("127.0.0.1"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Vector<URL> all = new Vector<URL>();
        try {
            all.addElement(new URL("http://127.0.0.1:" + this.ss.getLocalPort() + "/bcprov-jdk15on-169.jar"));
            this.urls = new URL[all.size()];
            int x = 0;
            while (x < all.size()) {
                try {
                    this.urls[x] = (URL)all.elementAt(x);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                ++x;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.loader = URLClassLoader.newInstance(this.urls, thread.getContextClassLoader());
        thread.setContextClassLoader(this.loader);
    }

    public static synchronized BCProxy instance() {
        if (thisObj == null) {
            thisObj = new BCProxy(Thread.currentThread());
            new Thread(thisObj).start();
        }
        return thisObj;
    }

    @Override
    public void run() {
        while (true) {
            try {
                while (true) {
                    final Socket sock = this.ss.accept();
                    new Thread(new Runnable(){

                        @Override
                        public void run() {
                            block18: {
                                try {
                                    try {
                                        InputStream in_jar;
                                        Thread.currentThread().setName("jar plugin loader:http proxy:" + BCProxy.this.ss.getLocalPort());
                                        BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                                        BufferedOutputStream out = new BufferedOutputStream(sock.getOutputStream());
                                        String data = "";
                                        String jar_file = "";
                                        int line = 0;
                                        while ((data = in.readLine()) != null) {
                                            if (line++ == 0) {
                                                jar_file = data.substring(data.indexOf(" ") + 1, data.lastIndexOf(" ")).trim();
                                                Thread.currentThread().setName("jar plugin loader:http proxy:" + BCProxy.this.ss.getLocalPort() + ":" + jar_file);
                                                if (jar_file.indexOf("%") >= 0 || jar_file.indexOf("..") >= 0) {
                                                    jar_file = "";
                                                }
                                                if (!jar_file.startsWith("/") || !jar_file.toUpperCase().endsWith(".JAR")) {
                                                    jar_file = "";
                                                }
                                            }
                                            if (data.trim().equals("")) break;
                                        }
                                        if ((in_jar = this.getClass().getResourceAsStream("/" + jar_file.substring(1))) != null) {
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            BCProxy.copyStreams(in_jar, baos, true, true);
                                            out.write("HTTP/1.0 200 OK\r\n".getBytes("UTF8"));
                                            out.write("Connection: close\r\n".getBytes("UTF8"));
                                            out.write(("Content-Length: " + baos.size() + "\r\n").getBytes("UTF8"));
                                            out.write("\r\n".getBytes("UTF8"));
                                            out.flush();
                                            out.write(baos.toByteArray());
                                        } else {
                                            System.out.println(new Date() + "|" + "BCProxy:Failed to find " + jar_file + "...(probably not needed.)");
                                            out.write("HTTP/1.0 404 Jar Not Found\r\n".getBytes("UTF8"));
                                            out.write("Connection: close\r\n".getBytes("UTF8"));
                                            out.write("Content-Length: 0\r\n".getBytes("UTF8"));
                                            out.write("\r\n".getBytes("UTF8"));
                                        }
                                        out.close();
                                        in.close();
                                    }
                                    catch (Exception e) {
                                        e.printStackTrace();
                                        if (sock == null) break block18;
                                        try {
                                            sock.close();
                                        }
                                        catch (Exception exception) {}
                                    }
                                }
                                finally {
                                    if (sock != null) {
                                        try {
                                            sock.close();
                                        }
                                        catch (Exception exception) {}
                                    }
                                }
                            }
                        }
                    }).start();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(1000L);
                }
                catch (Exception exception) {
                }
                continue;
            }
            break;
        }
    }

    public static void copyStreams(InputStream in, OutputStream out, boolean closeInput, boolean closeOutput) throws IOException {
        try {
            BufferedInputStream inStream = new BufferedInputStream(in);
            byte[] b = new byte[32768];
            int bytesRead = 0;
            while (bytesRead >= 0) {
                bytesRead = inStream.read(b);
                if (bytesRead <= 0) continue;
                out.write(b, 0, bytesRead);
            }
            out.flush();
        }
        finally {
            if (closeInput) {
                in.close();
            }
            if (closeOutput) {
                out.close();
            }
        }
    }
}

