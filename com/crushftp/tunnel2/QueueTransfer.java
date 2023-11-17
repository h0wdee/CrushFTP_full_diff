/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel2;

import com.crushftp.client.HttpURLConnection;
import com.crushftp.tunnel2.Chunk;
import com.crushftp.tunnel2.DProperties;
import com.crushftp.tunnel2.Queue;
import com.crushftp.tunnel2.Tunnel2;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

public class QueueTransfer
implements Runnable {
    Tunnel2 t = null;
    long transferred = 0L;
    String type = "";
    HttpURLConnection urlc = null;
    boolean keepRunning = true;
    boolean restartPlease = false;
    OutputStream out = null;
    InputStream in = null;
    long lastSend = System.currentTimeMillis();
    Vector threadAcks = new Vector();
    float slow_transfer = 0.0f;
    long startLoop = System.currentTimeMillis();
    long endLoop = System.currentTimeMillis();
    Vector reference_in_out = null;

    public QueueTransfer(Tunnel2 t, String type, Vector reference_in_out) {
        this.t = t;
        this.type = type;
        this.reference_in_out = reference_in_out;
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Tunnel QueueTransfer:" + this.type);
        do {
            this.restartPlease = false;
            try {
                try {
                    this.urlc = this.t.getSendGet(this.type.equals("send"));
                    this.urlc.getResponseCode();
                    if (this.type.equals("send")) {
                        this.doSend();
                    } else if (this.type.equals("get")) {
                        this.doGet();
                    }
                }
                catch (Exception e) {
                    Tunnel2.msg(e);
                    this.t.releaseUrl(this.urlc);
                    this.reference_in_out.remove(this);
                    continue;
                }
            }
            catch (Throwable throwable) {
                this.t.releaseUrl(this.urlc);
                this.reference_in_out.remove(this);
                throw throwable;
            }
            this.t.releaseUrl(this.urlc);
            this.reference_in_out.remove(this);
        } while (this.restartPlease);
        if (this.type.equals("send")) {
            this.t.co.removeOut(this);
        } else if (this.type.equals("get")) {
            this.t.co.removeIn(this);
        }
    }

    public long getTransferred() {
        long temp = this.transferred;
        this.transferred = 0L;
        return temp;
    }

    public void doSend() throws Exception {
        this.out = this.urlc.getOutputStream();
        while (this.keepRunning && this.t.isActive()) {
            if (!this.doSendLoop()) break;
        }
    }

    public boolean doSendLoop() throws Exception {
        if (!this.t.checkAcks()) {
            this.out.close();
            Tunnel2.msg("Tunnel2:Restarting send channel due to ack delays...");
            this.restartPlease = true;
            return false;
        }
        Chunk c = null;
        if (System.currentTimeMillis() - this.lastSend > 10000L) {
            c = this.t.makeCommand(0, "PINGSEND:" + System.currentTimeMillis());
            this.lastSend = System.currentTimeMillis();
        }
        if (c == null && ((float)this.threadAcks.size() < this.t.baseSpeedOut / 1024.0f / 4.0f || this.threadAcks.size() < 50)) {
            c = this.t.readLocal();
        }
        boolean ok = false;
        long start = System.currentTimeMillis();
        try {
            if (c != null) {
                byte[] b = c.toBytes();
                this.out.write(b);
                this.out.flush();
                this.transferred += (long)b.length;
                ok = true;
            }
        }
        catch (Exception e) {
            this.t.writeLocal(c, 0);
            Tunnel2.msg(e);
            return false;
        }
        int x = this.threadAcks.size() - 1;
        while (x >= 0) {
            if (!this.t.localAck.containsKey(this.threadAcks.elementAt(x).toString())) {
                this.threadAcks.removeElementAt(x);
            }
            --x;
        }
        if (c != null) {
            c.time = System.currentTimeMillis() - start < 100L ? System.currentTimeMillis() + 30000L : System.currentTimeMillis();
        }
        if (ok) {
            if (!c.isCommand() || c.getCommand().startsWith("CONNECT:") || c.getCommand().startsWith("END:") || c.getCommand().startsWith("CLOSEIN:")) {
                this.t.localAck.put(String.valueOf(c.num), c);
                this.threadAcks.addElement(String.valueOf(c.num));
            }
        } else {
            try {
                Thread.sleep(100L);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.endLoop = System.currentTimeMillis();
        return true;
    }

    public void doGet() throws Exception {
        this.in = this.urlc.getInputStream();
        while (this.keepRunning && this.t.isActive()) {
            Chunk c = null;
            try {
                Queue q;
                c = Chunk.parse(this.in);
                if (c == null) break;
                if (c.isCommand() && c.getCommand().startsWith("A:")) {
                    this.t.localAck.remove(c.getCommand().split(":")[1]);
                    c.b = DProperties.releaseArray(c.b);
                }
                if ((q = this.t.getQueue(c.id)) == null) {
                    Tunnel2.msg("Invalid queue:" + c.id + ":" + c.getCommand());
                    continue;
                }
                q.writeRemote(c);
                this.transferred += (long)(c.len + 12);
            }
            catch (Exception e) {
                if (e.getMessage().indexOf("Invalid chunk received") >= 0) {
                    this.t.reset();
                }
                Tunnel2.msg(e);
                return;
            }
            try {
                if (c != null) continue;
                Thread.sleep(100L);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    public void close() {
        this.keepRunning = false;
    }
}

