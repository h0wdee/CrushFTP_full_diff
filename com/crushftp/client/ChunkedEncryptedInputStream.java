/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.didisoft.pgp.PGPLib
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.didisoft.pgp.PGPLib;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChunkedEncryptedInputStream
extends InputStream {
    byte[] privateKeyBytes = null;
    String privateKeyPassword = null;
    ByteArrayOutputStream encryptedBytes = new ByteArrayOutputStream();
    ByteArrayOutputStream clearBytes = new ByteArrayOutputStream();
    ByteArrayInputStream clearIn = null;
    InputStream in = null;
    boolean closed = false;
    long pos = 0L;
    long endPos = -1L;
    boolean closeStream = false;
    boolean readheader = true;
    PGPLib pgp = new PGPLib();
    byte[] b1 = new byte[1];

    public ChunkedEncryptedInputStream(InputStream in, long endPos, String privateKey, String privateKeyPassword, boolean closeStream, boolean readheader) throws IOException {
        this.pgp.setUseExpiredKeys(true);
        this.in = in;
        this.endPos = endPos;
        this.closeStream = closeStream;
        this.readheader = readheader;
        this.privateKeyBytes = new byte[(int)new File(privateKey).length()];
        try (FileInputStream inFile = null;){
            inFile = new FileInputStream(new File(privateKey));
            inFile.read(this.privateKeyBytes);
        }
        this.privateKeyPassword = privateKeyPassword;
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
        int i = -1;
        while (i < 0) {
            byte[] chunk;
            if (this.clearIn != null) {
                i = this.clearIn.read(b, off, len);
            }
            if (i >= 0) continue;
            if (this.clearIn != null) {
                this.clearIn.close();
            }
            if (this.pos == 0L && this.readheader) {
                this.in.skip(Common.pgpChunkedheaderStr.length() + "0                                        ".length());
            }
            byte[] b1 = new byte[1];
            int bytes = -1;
            this.clearBytes.reset();
            do {
                if ((bytes = this.in.read(b1)) <= 0) continue;
                this.clearBytes.write(b1);
            } while (bytes >= 0 && b1[0] != 13);
            if (bytes < 0) {
                return -1;
            }
            String[] segment = new String(this.clearBytes.toByteArray(), "UTF8").split(":");
            int chunkSize = Integer.parseInt(segment[2].trim());
            int paddingSize = Integer.parseInt(segment[3].trim());
            this.clearBytes.reset();
            this.encryptedBytes.reset();
            while (chunkSize > 0) {
                chunk = new byte[chunkSize];
                bytes = this.in.read(chunk);
                if (bytes < 0) continue;
                this.encryptedBytes.write(chunk, 0, bytes);
                chunkSize -= bytes;
            }
            while (paddingSize > 0) {
                chunk = new byte[paddingSize];
                bytes = this.in.read(chunk);
                if (bytes < 0) continue;
                paddingSize -= bytes;
            }
            this.clearBytes.reset();
            this.decrypt(new ByteArrayInputStream(this.encryptedBytes.toByteArray()), this.privateKeyBytes, this.privateKeyPassword, this.clearBytes);
            this.clearIn = new ByteArrayInputStream(this.clearBytes.toByteArray());
        }
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
        this.closed = true;
    }

    public void decrypt(InputStream inStream, byte[] privateBytes, String privateKeyPassword, OutputStream outStream) throws IOException {
        try {
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(privateBytes);
            this.pgp.decryptStream(inStream, (InputStream)bytesIn, privateKeyPassword, outStream);
            bytesIn.close();
            inStream.close();
        }
        catch (Exception e) {
            throw new IOException(e);
        }
    }
}

