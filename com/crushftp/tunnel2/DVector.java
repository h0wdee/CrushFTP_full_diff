/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel2;

import com.crushftp.client.Common;
import com.crushftp.tunnel2.Chunk;
import com.crushftp.tunnel2.DProperties;
import com.crushftp.tunnel2.Tunnel2;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Properties;
import java.util.Vector;

public class DVector {
    Vector v = new Vector();
    boolean diskInitialized = false;
    RandomAccessFile tmp = null;
    String tmpName = null;
    Vector used = new Vector();
    Vector free = new Vector();
    long maxLoc = 0L;
    byte[] tmpB = DProperties.getArray();
    long bytes = 0L;
    boolean usingDisk = false;

    public long getBytes() {
        return this.bytes;
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
    public synchronized void insertElementAt(Chunk c, int i) throws IOException {
        this.bytes += (long)c.len;
        if (Tunnel2.ramUsage > Tunnel2.maxRam) {
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
                this.v.insertElementAt(String.valueOf(offset), i);
            }
        } else {
            if (this.usingDisk) {
                Tunnel2.msg("DV:Changing back to ram cache:" + Tunnel2.ramUsage + " < " + Tunnel2.maxRam);
            }
            this.usingDisk = false;
            this.v.insertElementAt(c, i);
            Tunnel2.addRam(c.len);
        }
    }

    public synchronized void addElement(Chunk c) throws IOException {
        this.insertElementAt(c, this.v.size());
    }

    public synchronized void addElement(Properties o) throws IOException {
        this.insertElementAt(new Chunk(Common.CLONE1(o)), this.v.size());
    }

    public synchronized void addElement(Vector o) throws IOException {
        this.insertElementAt(new Chunk(Common.CLONE1(o)), this.v.size());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized Chunk remove(int i) throws IOException {
        Object o = this.v.remove(i);
        Chunk c = null;
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            this.init();
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
        } else {
            c = (Chunk)o;
            Tunnel2.removeRam(c.len);
        }
        this.bytes -= (long)c.len;
        return c;
    }

    public synchronized Object removeItem(int i) throws IOException {
        Chunk c = this.remove(i);
        return Common.CLONE2(c.b);
    }

    public synchronized void setElementAt(Object o, int i) throws IOException {
        this.insertElementAt(new Chunk(Common.CLONE1(o)), i);
        this.remove(i + 1);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized Object elementAt(int i) throws IOException {
        Object o = this.v.get(i);
        Chunk c = null;
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            this.init();
            RandomAccessFile randomAccessFile = this.tmp;
            synchronized (randomAccessFile) {
                long offset = Long.parseLong(o.toString());
                this.tmp.seek(offset);
                this.tmp.readFully(this.tmpB);
                ByteArrayInputStream bis = new ByteArrayInputStream(this.tmpB);
                c = Chunk.parse(bis);
                bis.close();
            }
        } else {
            c = (Chunk)o;
        }
        return Common.CLONE2(c.b);
    }

    public int size() {
        return this.v.size();
    }

    public void close() {
        try {
            if (this.diskInitialized) {
                this.tmp.close();
                new File(this.tmpName).delete();
                this.diskInitialized = false;
                this.usingDisk = false;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.tmpB = DProperties.releaseArray(this.tmpB);
    }

    protected void finalize() throws Throwable {
        this.close();
    }
}

