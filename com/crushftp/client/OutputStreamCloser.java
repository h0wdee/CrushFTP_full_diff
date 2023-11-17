/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.GenericClient;
import com.crushftp.client.S3CrushClient;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Properties;

public class OutputStreamCloser
extends OutputStream {
    OutputStream out = null;
    OutputStream real_out = null;
    Properties status = null;
    long loc = 0L;
    boolean write_pgp_size_footer = false;
    boolean write_pgp_size_header = false;
    GenericClient c = null;
    String path = null;
    boolean closed = false;

    public OutputStreamCloser(OutputStream out, Properties status, GenericClient c, String path, boolean write_pgp_size_footer, boolean write_pgp_size_header, OutputStream real_out) throws IOException {
        this.out = out;
        this.real_out = real_out;
        this.status = status;
        this.c = c;
        this.path = path;
        this.write_pgp_size_footer = write_pgp_size_footer;
        this.write_pgp_size_header = write_pgp_size_header;
    }

    @Override
    public void flush() throws IOException {
        this.out.flush();
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
        this.out.write(b, start, len);
        this.loc += (long)len;
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.out.close();
        while (!this.status.containsKey("status")) {
            try {
                Thread.sleep(10L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!this.status.getProperty("status").equals("SUCCESS")) {
            throw new IOException((Exception)this.status.remove("error"));
        }
        try {
            if (this.write_pgp_size_footer) {
                this.real_out.write((":::" + System.getProperty("appname", "CrushFTP").toUpperCase() + "#" + this.loc).getBytes());
            }
        }
        catch (SocketException e) {
            // empty catch block
        }
        this.real_out.close();
        if (this.write_pgp_size_header && this.c != null && this.path != null) {
            try {
                this.c.doCommand("SITE PGP_HEADER_SIZE " + this.loc + " " + this.path);
            }
            catch (Exception e) {
                throw new IOException(e);
            }
        }
        if (this.c != null && this.c instanceof S3CrushClient) {
            try {
                ((S3CrushClient)this.c).setSize(this.path, this.loc);
            }
            catch (Exception e) {
                throw new IOException(e);
            }
        }
    }
}

