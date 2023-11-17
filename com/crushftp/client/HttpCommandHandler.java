/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Properties;

public class HttpCommandHandler {
    public SimpleDateFormat sdf_rfc1123 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);

    public void handleCommand(String path, Properties headers, Properties request, OutputStream out, InputStream in, Properties session, String ip) throws IOException {
    }

    public String processRequest(Properties request, ByteArrayOutputStream baos, Properties session, String ip, ByteArrayOutputStream tmp) throws Exception {
        return null;
    }

    public static void parseParams(String s, Properties request) {
        String[] s2 = s.split("&");
        int x = 0;
        while (x < s2.length) {
            request.put(Common.url_decode(s2[x].split("=")[0]).replace('+', ' '), Common.url_decode(s2[x].substring(s2[x].indexOf("=") + 1).replace('+', ' ')));
            ++x;
        }
    }

    public static String readLine(InputStream in) throws IOException {
        ByteArrayOutputStream s_tmp_bytes = new ByteArrayOutputStream();
        String s_tmp = "";
        int bytesRead = 0;
        byte[] b = new byte[1];
        while (bytesRead >= 0) {
            bytesRead = in.read(b);
            if (bytesRead > 0) {
                s_tmp = String.valueOf(s_tmp) + new String(b);
                s_tmp_bytes.write(b);
            }
            if (s_tmp.endsWith("\r\n")) break;
            if (bytesRead >= 0) continue;
            return null;
        }
        String data = new String(s_tmp_bytes.toByteArray(), "UTF8").trim();
        return data;
    }

    public void write_command_http(String s, OutputStream out) throws IOException {
        out.write(s.getBytes());
        out.write("\r\n".getBytes());
    }

    public ByteArrayOutputStream getRequest(Properties headers, Properties request, InputStream in) throws IOException {
        int max;
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        if (headers.containsKey("content-length") && (max = Integer.parseInt(headers.getProperty("content-length"))) > 0) {
            int bytesRead = 0;
            byte[] b = new byte[1024];
            long totalBytes = 0L;
            while (bytesRead >= 0 && totalBytes < (long)max) {
                if ((long)max - totalBytes < 1024L) {
                    b = new byte[(int)((long)max - totalBytes)];
                }
                if ((bytesRead = in.read(b)) <= 0 || (totalBytes += (long)bytesRead) >= 0x100000L) continue;
                tmp.write(b, 0, bytesRead);
            }
            HttpCommandHandler.parseParams(new String(tmp.toByteArray()), request);
        }
        return tmp;
    }
}

