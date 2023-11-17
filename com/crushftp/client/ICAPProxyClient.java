/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.Properties;

public class ICAPProxyClient
extends OutputStream {
    boolean closed = false;
    boolean icap_closed = false;
    boolean first_write = false;
    Socket sock = null;
    private String icap_host = null;
    private int icap_port = 0;
    String path = null;
    OutputStream real_out = null;
    private OutputStream icap_out = null;
    private InputStream icap_in = null;
    private String icap_service = null;
    private int preview_bytes = 30;
    String status = "";
    final int MAX_CHUNK_SIZE = 65535;
    final int MAX_HEADER_SIZE = 8192;
    long max_bytes_to_scan = 0x6400000L;
    long pos = 0L;

    public ICAPProxyClient(String icap_host, int icap_port, String icap_service, String path, OutputStream real_out, long max_bytes_to_scan) throws IOException {
        this.path = path;
        this.icap_service = icap_service;
        this.icap_host = icap_host;
        this.icap_port = icap_port;
        this.real_out = real_out;
        this.max_bytes_to_scan = max_bytes_to_scan;
    }

    private void firstWrite(byte[] first_chunk) throws IOException {
        this.sock = new Socket(this.icap_host, this.icap_port);
        this.icap_out = new BufferedOutputStream(this.sock.getOutputStream());
        this.icap_in = this.sock.getInputStream();
        Properties headers = this.parseHeader(this.getOptions());
        if (!headers.getProperty("StatusCode", "").equals("200")) {
            throw new IOException("Get options/preview failed:" + headers);
        }
        this.preview_bytes = Integer.parseInt(headers.getProperty("Preview", "30"));
        String head1 = "GET " + URLEncoder.encode(this.path, "UTF-8") + " HTTP/1.1\r\n";
        head1 = String.valueOf(head1) + "Host: " + this.icap_host + ":" + this.icap_port + "\r\n\r\n";
        String head2 = String.valueOf(head1) + "HTTP/1.1 200 OK\r\n";
        head2 = String.valueOf(head2) + "Transfer-Encoding: chunked\r\n";
        if (first_chunk.length < this.preview_bytes) {
            this.preview_bytes = first_chunk.length;
        }
        this.icap_out.write(("RESPMOD icap://" + this.icap_host + "/" + this.icap_service + " ICAP/1.0\r\n").getBytes("UTF-8"));
        this.icap_out.write(("Host: " + this.icap_host + "\r\n").getBytes("UTF-8"));
        this.icap_out.write("Connection:  close\r\n".getBytes("UTF-8"));
        this.icap_out.write("User-Agent: CrushFTP ICAP Client/1.1\r\n".getBytes("UTF-8"));
        this.icap_out.write("Allow: 204\r\n".getBytes("UTF-8"));
        this.icap_out.write(("Preview: " + this.preview_bytes + "\r\n").getBytes("UTF-8"));
        this.icap_out.write(("Encapsulated: req-hdr=0, res-hdr=" + head1.length() + ", res-body=" + head2.length() + "\r\n\r\n").getBytes("UTF-8"));
        this.icap_out.write((String.valueOf(head2) + Integer.toHexString(this.preview_bytes) + "\r\n").getBytes("UTF-8"));
        this.icap_out.write(first_chunk, 0, this.preview_bytes);
        this.icap_out.write("\r\n".getBytes("UTF-8"));
        if (first_chunk.length <= this.preview_bytes) {
            this.icap_out.write("0; ieof\r\n\r\n".getBytes("UTF-8"));
        } else if (this.preview_bytes != 0) {
            this.icap_out.write("0\r\n\r\n".getBytes("UTF-8"));
        }
        this.icap_out.flush();
        if (first_chunk.length > this.preview_bytes) {
            headers = this.parseHeader(this.getHeader("\r\n\r\n"));
            if (!headers.getProperty("StatusCode", "").equals("100")) {
                if (headers.getProperty("StatusCode", "").equals("200")) {
                    this.status = "ERROR:ICAP rejected";
                    throw new IOException(this.status);
                }
                if (headers.getProperty("StatusCode", "").equals("204")) {
                    this.status = "SUCCESS";
                    return;
                }
                if (headers.getProperty("StatusCode", "").equals("404")) {
                    this.status = "ERROR:ICAP 404 not found";
                    throw new IOException(this.status);
                }
                this.status = "ERROR:ICAP Server returned unknown status code:" + headers;
                throw new IOException(this.status);
            }
            byte[] b2 = new byte[first_chunk.length - this.preview_bytes];
            System.arraycopy(first_chunk, this.preview_bytes, b2, 0, b2.length);
            this.sendChunk(b2);
        }
    }

    private void sendChunk(byte[] chunk) throws IOException {
        byte[] b = new byte[65535];
        ByteArrayInputStream bin = new ByteArrayInputStream(chunk);
        int bytes_read = 0;
        while (bytes_read >= 0) {
            bytes_read = bin.read(b);
            if (bytes_read < 0) continue;
            this.icap_out.write((String.valueOf(Integer.toHexString(bytes_read)) + "\r\n").getBytes("UTF-8"));
            this.icap_out.write(b, 0, bytes_read);
            this.icap_out.write("\r\n".getBytes("UTF-8"));
        }
    }

    private String getOptions() throws IOException {
        String head = "OPTIONS icap://" + this.icap_host + "/" + this.icap_service + " ICAP/1.0\r\n";
        head = String.valueOf(head) + "Host: " + this.icap_host + "\r\n";
        head = String.valueOf(head) + "User-Agent: CrushFTP ICAP Client/1.1\r\n";
        head = String.valueOf(head) + "Encapsulated: null-body=0\r\n\r\n";
        this.icap_out.write(head.getBytes("UTF-8"));
        this.icap_out.flush();
        return this.getHeader("\r\n\r\n");
    }

    private String getHeader(String end_marker) throws IOException {
        int bytesRead = 0;
        byte[] b = new byte[1];
        String s = "";
        while (s.length() < 8192 && s.indexOf(end_marker) < 0 && bytesRead >= 0) {
            bytesRead = this.icap_in.read(b);
            if (bytesRead < 0) continue;
            s = String.valueOf(s) + new String(b, "UTF8");
        }
        return s;
    }

    private Properties parseHeader(String response) throws IOException {
        Properties p = new Properties();
        p.put("StatusCode", response.split(" ")[1]);
        BufferedReader br = new BufferedReader(new StringReader(response));
        String line = "";
        while ((line = br.readLine()) != null) {
            if (line.indexOf(":") < 0) continue;
            p.put(line.substring(0, line.indexOf(":")).trim(), line.substring(line.indexOf(":") + 1).trim());
        }
        return p;
    }

    public String getResultStatus() {
        return this.status;
    }

    @Override
    public void flush() throws IOException {
        this.icap_out.flush();
        this.real_out.flush();
    }

    @Override
    public void write(int i) throws IOException {
        this.write(new byte[]{(byte)i}, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int start, int len) throws IOException {
        if (this.closed) {
            throw new IOException("Already closed!");
        }
        if (this.status.equals("SUCCESS")) {
            this.real_out.write(b, start, len);
            return;
        }
        if (!this.status.equals("")) {
            throw new IOException(this.status);
        }
        byte[] b2 = new byte[len - start];
        System.arraycopy(b, start, b2, 0, len);
        if (!this.first_write) {
            this.first_write = true;
            this.firstWrite(b2);
        } else {
            this.sendChunk(b2);
        }
        this.real_out.write(b2);
        this.pos += (long)b2.length;
        if (this.pos > this.max_bytes_to_scan) {
            this.closeICAP();
        }
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        try {
            if (this.status.equals("") && !this.icap_closed) {
                this.closeICAP();
            }
            if (!this.status.equals("SUCCESS")) {
                throw new IOException(this.status);
            }
        }
        finally {
            this.closeICAP();
            try {
                this.real_out.close();
            }
            catch (IOException iOException) {}
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void closeICAP() throws IOException {
        if (this.icap_closed) {
            return;
        }
        this.icap_closed = true;
        try {
            this.icap_out.write("0\r\n\r\n".getBytes("UTF-8"));
            this.flush();
            String response = this.getHeader("\r\n\r\n");
            Properties headers = this.parseHeader(response);
            if (headers.getProperty("StatusCode", "").equals("204")) {
                this.status = "SUCCESS";
                return;
            }
            if (headers.getProperty("StatusCode", "").equals("200")) {
                response = this.getHeader("0\r\n\r\n");
                int resp_code = Integer.parseInt(response.split(" ")[1].trim());
                if (resp_code >= 200 && resp_code <= 299) {
                    this.status = "SUCCESS";
                    return;
                }
                if (resp_code == 403) {
                    this.status = "ERROR:" + response.substring(0, response.indexOf("\r")).trim();
                    throw new IOException(this.status);
                }
            }
            this.status = "ERROR:ICAP Unknown response:" + response;
            throw new IOException(this.status);
        }
        finally {
            try {
                this.icap_in.close();
            }
            catch (IOException iOException) {}
            try {
                this.icap_out.close();
            }
            catch (IOException iOException) {}
            try {
                this.sock.close();
            }
            catch (IOException iOException) {}
        }
    }
}

