/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Worker;
import com.didisoft.pgp.PGPLib;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class EncryptedInputStream
extends InputStream {
    byte[] privateKeyBytes = null;
    String privateKeyPassword = null;
    InputStream in = null;
    boolean closed = false;
    long pos = 0L;
    long endPos = -1L;
    boolean closeStream = false;
    PGPLib pgp = new PGPLib();
    byte[] b1 = new byte[1];
    Socket sock1 = null;
    Socket sock2 = null;

    public EncryptedInputStream(final InputStream in2, long endPos, String privateKey, String privateKeyPassword2, boolean closeStream) throws IOException {
        this.pgp.setUseExpiredKeys(true);
        this.endPos = endPos;
        this.closeStream = closeStream;
        this.privateKeyBytes = new byte[(int)new File(privateKey).length()];
        try (FileInputStream inFile = null;){
            inFile = new FileInputStream(new File(privateKey));
            inFile.read(this.privateKeyBytes);
        }
        this.privateKeyPassword = privateKeyPassword2;
        try (ServerSocket ss = new ServerSocket(0);){
            this.sock1 = new Socket("127.0.0.1", ss.getLocalPort());
            this.sock2 = ss.accept();
        }
        this.in = this.sock1.getInputStream();
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    EncryptedInputStream.this.pgp.decryptStream(in2, new ByteArrayInputStream(EncryptedInputStream.this.privateKeyBytes), EncryptedInputStream.this.privateKeyPassword, EncryptedInputStream.this.sock2.getOutputStream());
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    in2.close();
                    EncryptedInputStream.this.sock2.close();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, String.valueOf(Thread.currentThread().getName()) + "_EncryptedInputStream");
    }

    @Override
    public int read() throws IOException {
        int bytesRead = this.in.read(this.b1, 0, 1);
        if (bytesRead >= 0) {
            ++this.pos;
            return this.b1[0] & 0xFF;
        }
        return -1;
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (this.pos >= this.endPos && this.endPos >= 0L) {
            return -1;
        }
        int i = this.in.read(b, off, len);
        if (i > 0) {
            this.pos += (long)i;
        }
        if (this.endPos > 0L && this.pos > this.endPos) {
            i = (int)((long)i - (this.pos - this.endPos));
        }
        return i;
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }
        if (this.closeStream) {
            this.in.close();
        }
        this.sock1.close();
        this.sock2.close();
        this.closed = true;
    }
}

