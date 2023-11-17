/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel3;

import com.crushftp.client.HttpURLConnection;
import com.crushftp.tunnel2.Chunk;
import com.crushftp.tunnel3.StreamController;
import java.io.OutputStream;
import java.util.Vector;

public class StreamWriter
implements Runnable {
    StreamController sc = null;
    HttpURLConnection urlc = null;
    OutputStream out = null;
    public boolean close = false;
    String channel_id = null;
    long bytes = 0L;
    public Object byte_lock = new Object();
    public long last_write = System.currentTimeMillis();
    Vector history = new Vector();

    public StreamWriter(StreamController sc, HttpURLConnection urlc, String channel_id) {
        this.sc = sc;
        this.urlc = urlc;
        this.channel_id = channel_id;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        Chunk c = null;
        while (!this.close && this.sc.isActive()) {
            Thread.currentThread().setName(this.sc + ":StreamWriter:" + this.channel_id);
            if (this.urlc == null) {
                this.urlc = this.sc.addTransport(true, this.channel_id);
            }
            if (this.urlc == null) break;
            this.urlc.setAllowPool(false);
            try {
                if (this.out == null) {
                    this.out = this.urlc.getOutputStream();
                }
                if (this.close) break;
                c = this.processOutgoingChunk();
                if (this.close) break;
                if (c != null) {
                    if (c.isCommand()) {
                        this.sc.msg("SENDING CMD :" + c);
                    }
                    c.sw = this;
                    this.out.write(c.toBytes());
                    this.out.flush();
                    Object object = this.byte_lock;
                    synchronized (object) {
                        this.bytes += (long)c.len;
                    }
                    this.sc.addBytes(c.id, c.len);
                    this.last_write = System.currentTimeMillis();
                    this.sc.updateStats(c, this.channel_id, this.history, "write", this.sc.outgoing.indexOf(this));
                    this.sc.last_send_activity = System.currentTimeMillis();
                } else {
                    Thread.sleep(100L);
                }
                if (!this.close) continue;
                break;
            }
            catch (Exception e) {
                if (c != null && !c.getCommand().startsWith("PING")) {
                    c.time = System.currentTimeMillis();
                    this.sc.localCache.remove(String.valueOf(c.id) + ":" + c.num);
                    if (c.isCommand()) {
                        this.sc.out_queue_commands.insertElementAt(c, 0);
                    } else {
                        this.sc.getQueue(String.valueOf(c.id)).insertElementAt(c, 0);
                    }
                }
                this.sc.msg(e);
                this.reset();
            }
        }
        this.sc.outgoing.remove(this);
        this.reset();
        this.sc.stats.remove(String.valueOf(this.channel_id) + ":write");
    }

    public void close() {
        this.close = true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void reset() {
        try {
            this.out.close();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            this.urlc.disconnect();
        }
        catch (Exception exception) {
            // empty catch block
        }
        try {
            Object object = this.sc.bind_lock;
            synchronized (object) {
                if (this.urlc != null && this.urlc.getBindIp() != null) {
                    this.sc.out_binds.put(this.urlc.getBindIp(), String.valueOf(Integer.parseInt(this.sc.out_binds.getProperty(this.urlc.getBindIp(), "1")) - 1));
                }
            }
        }
        catch (Exception e) {
            this.sc.msg(e);
        }
        this.urlc = null;
        this.out = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public long getTransferred() {
        Object object = this.byte_lock;
        synchronized (object) {
            long bytes2 = this.bytes;
            this.bytes = 0L;
            return bytes2;
        }
    }

    public String getBindIp() {
        if (this.urlc != null) {
            return this.urlc.getBindIp();
        }
        return "0.0.0.0";
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Chunk processOutgoingChunk() throws InterruptedException {
        Chunk c = null;
        if (this.sc == null) {
            return null;
        }
        if (this.sc.closeRequests.containsKey(this.channel_id)) {
            c = this.sc.makeCommand(0, "CLOSE:" + this.channel_id);
        } else {
            Object object = this.sc.out_queue_remove;
            synchronized (object) {
                int queue_count;
                block12: {
                    c = null;
                    int loops = 0;
                    queue_count = this.sc.getQueueCount();
                    while (queue_count == 0 && this.sc.out_queue_commands.size() == 0 && c == null && !this.close) {
                        Thread.sleep(100L);
                        if (loops++ > 10) break;
                        if (System.currentTimeMillis() - this.last_write > 10000L) {
                            c = this.sc.makeCommand(0, "PINGSEND:" + System.currentTimeMillis());
                        }
                        queue_count = this.sc.getQueueCount();
                    }
                    if (!this.close) break block12;
                    return null;
                }
                if (this.sc.out_queue_commands.size() > 0 && c == null && !(c = (Chunk)this.sc.out_queue_commands.remove(0)).getCommand().startsWith("A:")) {
                    this.sc.localCache.put(String.valueOf(c.id) + ":" + c.num, c);
                }
                if (queue_count > 0 && c == null && (c = this.sc.popOut()) != null) {
                    this.sc.localCache.put(String.valueOf(c.id) + ":" + c.num, c);
                }
                if (c != null) {
                    c.time = System.currentTimeMillis();
                }
            }
        }
        return c;
    }
}

