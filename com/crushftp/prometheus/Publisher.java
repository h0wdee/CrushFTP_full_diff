/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.prometheus;

import com.crushftp.client.HTTPD;
import com.crushftp.client.HttpCommandHandler;
import com.crushftp.client.Worker;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

public class Publisher
extends HttpCommandHandler {
    public static Properties last_info = new Properties();

    public static void main(String[] args) {
        final String[] args2 = args;
        System.setProperty("crushftp.worker.v8", "true");
        System.setProperty("com.crushftp.server.httphandler", "com.crushftp.prometheus.Publisher");
        System.setProperty("crushftp.agent.port", "9191");
        try {
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    HTTPD.main(args2);
                }
            });
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleCommand(String path, Properties headers, Properties request, OutputStream out, InputStream in, Properties session, String ip) throws IOException {
        String contentType = "text/plain";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ByteArrayOutputStream tmp = this.getRequest(headers, request, in);
            contentType = this.processRequest(request, baos, session, ip, tmp);
        }
        catch (Exception e) {
            e.printStackTrace();
            baos.write(("" + e).getBytes());
        }
        this.write_command_http("HTTP/1.1 200 OK", out);
        this.write_command_http("Date: " + this.sdf_rfc1123.format(new Date()), out);
        this.write_command_http("Server: CrushHTTPD", out);
        this.write_command_http("P3P: policyref=\"p3p.xml\", CP=\"IDC DSP COR ADM DEVi TAIi PSA PSD IVAi IVDi CONi HIS OUR IND CNT\"", out);
        this.write_command_http("Keep-Alive: timeout=15, max=20", out);
        this.write_command_http("Content-Type: " + contentType, out);
        this.write_command_http("Connection: Keep-Alive", out);
        this.write_command_http("Last-Modified: " + this.sdf_rfc1123.format(new Date()), out);
        this.write_command_http("ETag: " + System.currentTimeMillis(), out);
        this.write_command_http("Content-Length: " + baos.size(), out);
        this.write_command_http("", out);
        out.write(baos.toByteArray());
        out.flush();
    }

    @Override
    public String processRequest(Properties request, ByteArrayOutputStream baos, Properties session, String ip, ByteArrayOutputStream tmp) throws Exception {
        String contentType = "text/html";
        File[] list = new File("/home/").listFiles();
        int x = 0;
        while (list != null && x < list.length) {
            if (list[x].getName().startsWith("prometheus") && list[x].getName().endsWith(".txt")) {
                RandomAccessFile raf = new RandomAccessFile(list[x].getPath(), "r");
                byte[] b = new byte[(int)raf.length()];
                raf.readFully(b);
                raf.close();
                list[x].delete();
                String s = new String(b).trim();
                last_info.put(list[x].getName(), s);
            }
            ++x;
        }
        Enumeration<Object> keys = last_info.keys();
        while (keys.hasMoreElements()) {
            String key = "" + keys.nextElement();
            baos.write(last_info.getProperty(key).getBytes());
            baos.write("\n\n".getBytes());
        }
        return contentType;
    }
}

