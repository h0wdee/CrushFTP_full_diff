/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.client;

import com.crushftp.client.Common;
import com.crushftp.client.File_U;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Properties;

public class RandomOutputStream
extends OutputStream {
    public static final Properties sharedOutput = new Properties();
    public static final Properties sharedOutputWho = new Properties();
    public static final Properties sharedOutputCount = new Properties();
    String filePath = null;
    RandomAccessFile raf = null;
    Object rafLock = new Object();
    Object pendingLock = new Object();
    int pending = 0;
    long pos = 0L;
    boolean shared = false;
    public MessageDigest md5 = null;
    public String lastMd5 = "";
    long bytes_written = 0L;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public RandomOutputStream(File f, boolean shared) throws IOException {
        this.filePath = f.getCanonicalPath();
        this.shared = shared;
        try {
            if (this.md5 == null && System.getProperty("crushftp.stor_md5", "true").equals("true")) {
                this.md5 = MessageDigest.getInstance(System.getProperty("crushftp.hash_algorithm", "MD5"));
            }
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmException) {
            // empty catch block
        }
        if (this.md5 != null) {
            this.md5.reset();
        }
        this.raf = (RandomAccessFile)sharedOutput.get(this.filePath);
        if (!shared && this.raf != null) {
            throw new IOException("RandomOutputStream:File in use still:" + f.getName() + ":" + sharedOutputWho.getProperty(this.filePath));
        }
        if (this.raf == null) {
            this.raf = new RandomAccessFile(new File_U(this.filePath), "rw");
        }
        sharedOutput.put(this.filePath, this.raf);
        sharedOutputWho.put(this.filePath, String.valueOf(Thread.currentThread().getName()) + ":" + new Date());
        Object object = this.rafLock;
        synchronized (object) {
            sharedOutputCount.put(this.filePath, String.valueOf(Integer.parseInt(sharedOutputCount.getProperty(this.filePath, "0")) + 1));
        }
        this.pos = this.raf.getFilePointer();
        this.bytes_written = 0L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void append() throws IOException {
        if (this.raf == null) {
            return;
        }
        Object object = this.rafLock;
        synchronized (object) {
            this.seek(this.length());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setLength(long l) throws IOException {
        if (this.raf == null) {
            return;
        }
        Object object = this.rafLock;
        synchronized (object) {
            this.raf.setLength(l);
            this.pos = this.raf.getFilePointer();
        }
    }

    @Override
    public void flush() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void seek(long l) throws IOException {
        if (this.raf == null) {
            return;
        }
        Object object = this.rafLock;
        synchronized (object) {
            this.raf.seek(l);
            this.pos = this.raf.getFilePointer();
        }
    }

    public long length() throws IOException {
        return this.raf.length();
    }

    @Override
    public void write(int i) throws IOException {
        this.write(new byte[]{(byte)i}, 0, 1);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.write(b, 0, b.length);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void write(byte[] b, int start, int len) throws IOException {
        if (this.raf == null) {
            return;
        }
        Object object = this.pendingLock;
        synchronized (object) {
            ++this.pending;
        }
        object = this.rafLock;
        synchronized (object) {
            if (this.raf == null) {
                throw new IOException(String.valueOf(this.filePath) + " has been closed.");
            }
            if (this.md5 != null) {
                this.md5.update(b, start, len);
            }
            this.raf.seek(this.pos);
            this.raf.write(b, start, len);
            this.pos = this.raf.getFilePointer();
            this.bytes_written += (long)len;
        }
        object = this.pendingLock;
        synchronized (object) {
            --this.pending;
        }
    }

    public String getLastMd5() {
        return this.lastMd5;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() throws IOException {
        if (this.raf == null) {
            return;
        }
        long start = System.currentTimeMillis();
        while (this.pending > 0 && System.currentTimeMillis() - start < 30000L) {
            try {
                Thread.sleep(1L);
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        boolean timeoutOnClose = System.currentTimeMillis() - start > 30000L;
        Object object = this.rafLock;
        synchronized (object) {
            int i = Integer.parseInt(sharedOutputCount.getProperty(this.filePath, "0")) - 1;
            if (i <= 0 || !this.shared) {
                sharedOutputCount.remove(this.filePath);
                Common.log("SERVER", 2, "RandomOutputStream:" + this.filePath + " bytes_written=" + this.bytes_written + " pos=" + this.pos + " filePointer=" + this.raf.getFilePointer());
                this.raf.close();
                sharedOutput.remove(this.filePath);
                sharedOutputWho.remove(this.filePath);
            } else {
                sharedOutputCount.put(this.filePath, String.valueOf(i));
            }
            this.raf = null;
            if (this.md5 != null) {
                this.lastMd5 = new BigInteger(1, this.md5.digest()).toString(16).toLowerCase();
            }
            while (this.lastMd5.length() < 32) {
                this.lastMd5 = "0" + this.lastMd5;
            }
        }
        if (timeoutOnClose) {
            throw new IOException("RandomOutputStream timeout of 30 seconds on close..file is not likely complete!");
        }
    }
}

