/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.didisoft.pgp.PGPLib
 */
package com.crushftp.client;

import com.crushftp.client.Worker;
import com.didisoft.pgp.PGPLib;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class EncryptedOutputStream
extends OutputStream {
    OutputStream out = null;
    byte[] publicKeyBytes = null;
    boolean closed = false;
    boolean closeStream = false;
    PGPLib pgp = new PGPLib();
    byte[] b1 = new byte[1];
    Socket sock1 = null;
    Socket sock2 = null;

    public EncryptedOutputStream(final OutputStream out2, String publicKey, boolean closeStream) throws IOException {
        this.pgp.setUseExpiredKeys(true);
        this.closeStream = closeStream;
        this.publicKeyBytes = new byte[(int)new File(publicKey).length()];
        try (FileInputStream inFile = null;){
            inFile = new FileInputStream(new File(publicKey));
            inFile.read(this.publicKeyBytes);
        }
        try (ServerSocket ss = new ServerSocket(0);){
            this.sock1 = new Socket("127.0.0.1", ss.getLocalPort());
            this.sock2 = ss.accept();
        }
        this.out = this.sock1.getOutputStream();
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    EncryptedOutputStream.this.pgp.encryptStream(EncryptedOutputStream.this.sock2.getInputStream(), "", (InputStream)new ByteArrayInputStream(EncryptedOutputStream.this.publicKeyBytes), out2, true, false);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    out2.close();
                    EncryptedOutputStream.this.sock2.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, String.valueOf(Thread.currentThread().getName()) + "_EncryptedOutputStream");
    }

    @Override
    public void write(int i) throws IOException {
        this.b1[0] = (byte)i;
        this.write(this.b1, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.out.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        this.out.flush();
        if (this.closeStream) {
            this.out.close();
        }
        this.sock1.close();
        this.sock2.close();
        this.closed = true;
    }
}

