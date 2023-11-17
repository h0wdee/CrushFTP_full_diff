/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel3;

import com.crushftp.client.HttpURLConnection;
import com.crushftp.tunnel2.Chunk;
import com.crushftp.tunnel3.Stream;
import com.crushftp.tunnel3.StreamController;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Vector;

public class StreamReader
implements Runnable {
    StreamController sc = null;
    HttpURLConnection urlc = null;
    InputStream in = null;
    String channel_id = null;
    long bytes = 0L;
    public Object byte_lock = new Object();
    Vector history = new Vector();

    public StreamReader(StreamController sc, HttpURLConnection urlc, String channel_id) {
        this.sc = sc;
        this.urlc = urlc;
        this.channel_id = channel_id;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        while (this.sc.isActive()) {
            Thread.currentThread().setName(this.sc + ":StreamReader:" + this.channel_id);
            boolean new_stream = false;
            if (this.urlc == null) {
                this.urlc = this.sc.addTransport(false, this.channel_id);
                new_stream = true;
            }
            if (this.urlc == null) break;
            this.urlc.setAllowPool(false);
            try {
                Chunk c;
                if (this.in == null) {
                    this.in = this.urlc.getInputStream();
                }
                if ((c = Chunk.parse(this.in)) != null) {
                    Object object = this.byte_lock;
                    synchronized (object) {
                        this.bytes += (long)c.len;
                    }
                    if (StreamReader.processIncomingChunk(this.sc, c)) {
                        break;
                    }
                } else if (new_stream) {
                    this.sc.st.noIncomingCount = this.urlc.getResponseCode() == 302 && this.sc.isActive() ? (this.sc.st.noIncomingCount += 100) : ++this.sc.st.noIncomingCount;
                } else {
                    throw new IOException("Channel stream closed:" + this.channel_id);
                }
                this.sc.updateStats(c, this.channel_id, this.history, "read", this.sc.incoming.indexOf(this));
            }
            catch (Exception e) {
                this.sc.msg(e);
                break;
            }
        }
        try {
            this.urlc.disconnect();
        }
        catch (Exception exception) {
            // empty catch block
        }
        Object object = this.sc.bind_lock;
        synchronized (object) {
            if (this.urlc != null && this.urlc.getBindIp() != null) {
                this.sc.in_binds.put(this.urlc.getBindIp(), String.valueOf(Integer.parseInt(this.sc.in_binds.getProperty(this.urlc.getBindIp(), "1")) - 1));
            }
        }
        this.urlc = null;
        this.in = null;
        this.sc.incoming.remove(this);
        this.sc.stats.remove(String.valueOf(this.channel_id) + ":read");
    }

    public static boolean processIncomingChunk(StreamController sc, Chunk c) throws InterruptedException {
        if (c.isCommand()) {
            sc.msg("RECEIVED CMD :" + c);
            return sc.processCommand(c, (Stream)sc.streams.get(String.valueOf(c.id)));
        }
        sc.last_receive_activity = System.currentTimeMillis();
        Properties queue = null;
        int x = 0;
        while (x < 500 && queue == null) {
            queue = (Properties)sc.in_queues.get(String.valueOf(c.id));
            if (queue == null && sc.bad_queues.containsKey(String.valueOf(c.id))) break;
            if (queue == null) {
                Thread.sleep(10L);
            }
            ++x;
        }
        if (queue != null) {
            queue.put(String.valueOf(c.num), c);
        } else {
            if (sc.bad_queues.size() > 100) {
                sc.bad_queues.clear();
            }
            sc.bad_queues.put(String.valueOf(c.id), "");
            sc.out_queue_commands.insertElementAt(sc.makeCommand(c.id, "A:" + c.num), 0);
        }
        return false;
    }

    public void close() {
        this.sc.getQueue("unknown").insertElementAt(this.sc.makeCommand(0, "CLOSE:" + this.channel_id), 0);
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
}

