/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel2;

import com.crushftp.client.Common;
import com.crushftp.tunnel2.Chunk;
import com.crushftp.tunnel2.Tunnel2;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class DProperties {
    static Vector freeBytes = new Vector();
    Properties p = new Properties();
    boolean diskInitialized = false;
    RandomAccessFile tmp = null;
    String tmpName = null;
    Vector used = new Vector();
    Vector free = new Vector();
    long maxLoc = 0L;
    byte[] tmpB = DProperties.getArray();
    boolean usingDisk = false;
    static int chunkSize = 65548;
    static byte[] zeros = new byte[chunkSize];

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static byte[] getArray() {
        Vector vector = freeBytes;
        synchronized (vector) {
            if (freeBytes.size() > 0) {
                return (byte[])freeBytes.remove(0);
            }
            return new byte[chunkSize];
        }
    }

    public static byte[] releaseArray(byte[] b) {
        if (b == null) {
            return null;
        }
        if (b.length != chunkSize) {
            return null;
        }
        System.arraycopy(zeros, 0, b, 0, b.length);
        freeBytes.addElement(b);
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Chunk get(String key) throws IOException {
        Object o = this.p.get(key);
        if (o instanceof String) {
            this.init();
            Chunk c = null;
            RandomAccessFile randomAccessFile = this.tmp;
            synchronized (randomAccessFile) {
                long offset = Long.parseLong(o.toString());
                this.tmp.seek(offset);
                this.tmp.readFully(this.tmpB);
                ByteArrayInputStream bis = new ByteArrayInputStream(this.tmpB);
                c = Chunk.parse(bis);
                bis.close();
            }
            return c;
        }
        return (Chunk)o;
    }

    public synchronized void init() throws IOException {
        this.usingDisk = true;
        if (this.diskInitialized) {
            return;
        }
        this.tmpName = String.valueOf(System.getProperty("crushftp.tunnel.temp.path", System.getProperty("java.io.tmpdir"))) + "Tunnel2_" + System.currentTimeMillis() + "_" + Common.makeBoundary(5) + ".tmp";
        Tunnel2.msg("Ram cache exceeded, using scratch file on disk:" + this.tmpName + "   " + Tunnel2.ramUsage + " > " + Tunnel2.maxRam + " Free JVM Memory:" + Common.format_bytes_short(Common.getFreeRam()));
        this.tmp = new RandomAccessFile(this.tmpName, "rw");
        this.tmp.setLength(0L);
        this.diskInitialized = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized void put(String key, Chunk c) throws IOException {
        if (Tunnel2.ramUsage <= Tunnel2.maxRam || !this.usingDisk) {
            // empty if block
        }
        if (Tunnel2.ramUsage > Tunnel2.maxRam || Common.getFreeRam() < 0x2000000L) {
            this.init();
            RandomAccessFile randomAccessFile = this.tmp;
            synchronized (randomAccessFile) {
                long offset = 0L;
                if (this.free.size() == 0) {
                    this.free.addElement(String.valueOf(this.maxLoc));
                    this.maxLoc += (long)this.tmpB.length;
                }
                offset = Long.parseLong(this.free.remove(0).toString());
                this.used.addElement(String.valueOf(offset));
                this.tmp.seek(offset);
                byte[] b = c.toBytes();
                System.arraycopy(b, 0, this.tmpB, 0, b.length);
                this.tmp.write(this.tmpB);
                this.p.put(key, String.valueOf(offset));
            }
        } else {
            if (this.usingDisk) {
                Tunnel2.msg("DP:Changing back to ram cache:" + Tunnel2.ramUsage + " < " + Tunnel2.maxRam);
            }
            this.usingDisk = false;
            this.p.put(key, c);
            Tunnel2.addRam(c.len);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized Chunk remove(String key) throws IOException {
        Object o = this.p.remove(key);
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            this.init();
            Chunk c = null;
            RandomAccessFile randomAccessFile = this.tmp;
            synchronized (randomAccessFile) {
                long offset = Long.parseLong(o.toString());
                this.tmp.seek(offset);
                this.tmp.readFully(this.tmpB);
                ByteArrayInputStream bis = new ByteArrayInputStream(this.tmpB);
                c = Chunk.parse(bis);
                bis.close();
                this.used.remove(String.valueOf(offset));
                this.free.addElement(String.valueOf(offset));
            }
            return c;
        }
        Chunk c = (Chunk)o;
        Tunnel2.removeRam(c.len);
        return c;
    }

    public int size() {
        return this.p.size();
    }

    public boolean containsKey(String key) {
        return this.p.containsKey(key);
    }

    public Enumeration keys() {
        return this.p.keys();
    }

    public synchronized void close() {
        try {
            if (this.diskInitialized) {
                this.tmp.close();
                new File(this.tmpName).delete();
                this.diskInitialized = false;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        Enumeration keys = this.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            Object o = this.p.remove(key);
            if (o instanceof String) continue;
            Chunk c = (Chunk)o;
            Tunnel2.removeRam(c.len);
        }
        this.tmpB = DProperties.releaseArray(this.tmpB);
        this.used.clear();
        this.free.clear();
    }
}

