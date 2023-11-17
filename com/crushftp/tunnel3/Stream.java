/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel3;

import com.crushftp.client.Common;
import com.crushftp.client.Worker;
import com.crushftp.tunnel2.Chunk;
import com.crushftp.tunnel3.StreamController;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class Stream
implements Runnable {
    Socket sock = null;
    int id = -1;
    StreamController sc = null;
    Stream thisObj = this;
    int parent_stream_id = 0;
    int last_num = -1;
    int local_num = 1;
    int remote_num = 1;
    long last_activity = System.currentTimeMillis();
    OutputStream out = null;
    InputStream in = null;
    Properties tunnel = null;
    boolean remote_killed = false;
    boolean local_killed = false;
    public long ram_allocated_out = 0x100000 * Integer.parseInt(System.getProperty("crushftp.tunnel_ram_cache", "1"));
    StringBuffer buffer_tuner_status = new StringBuffer();

    public Stream(Socket sock, int id, StreamController sc, Properties tunnel, int parent_stream_id) {
        this.sock = sock;
        this.id = id;
        this.sc = sc;
        this.tunnel = tunnel;
        this.parent_stream_id = parent_stream_id;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void run() {
        block54: {
            block53: {
                StreamController.addRamAllocated(this.ram_allocated_out);
                try {
                    try {
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                Thread.currentThread().setName("Tunnel3 stream " + Stream.this.id + " writer to " + Stream.this.sock);
                                try {
                                    Stream parent_st = null;
                                    if (Stream.this.parent_stream_id > 0) {
                                        parent_st = (Stream)Stream.this.sc.streams.get(String.valueOf(Stream.this.parent_stream_id));
                                    }
                                    Properties queue = (Properties)Stream.this.sc.in_queues.get(String.valueOf(Stream.this.id));
                                    Stream.this.out = Stream.this.sock.getOutputStream();
                                    long last_master_ack = System.currentTimeMillis();
                                    int last_ack_num = 0;
                                    long bytes_pending_ack = 0L;
                                    long ram_max_out_window = 0x100000 * Integer.parseInt(Stream.this.tunnel.getProperty("sendBuffer", "1"));
                                    while ((Stream.this.remote_num < Stream.this.last_num || Stream.this.last_num < 0) && !Stream.this.sock.isClosed() && Stream.this.sc.isActive()) {
                                        if (queue.containsKey(String.valueOf(Stream.this.remote_num))) {
                                            Chunk c = (Chunk)queue.remove(String.valueOf(Stream.this.remote_num++));
                                            bytes_pending_ack += (long)c.len;
                                            Stream.this.out.write(c.b, 0, c.len);
                                            Stream.this.out.flush();
                                            Stream.this.last_activity = System.currentTimeMillis();
                                            if (parent_st != null) {
                                                parent_st.last_activity = Stream.this.last_activity;
                                            }
                                        } else {
                                            Thread.sleep(100L);
                                        }
                                        if (System.currentTimeMillis() - last_master_ack <= 1000L && bytes_pending_ack <= ram_max_out_window) continue;
                                        if (last_ack_num < Stream.this.remote_num) {
                                            Stream.this.sc.out_queue_commands.insertElementAt(Stream.this.sc.makeCommand(Stream.this.id, "A:M:" + Stream.this.remote_num), 0);
                                        }
                                        last_ack_num = Stream.this.remote_num;
                                        bytes_pending_ack = 0L;
                                        last_master_ack = System.currentTimeMillis();
                                    }
                                    Stream.this.out.close();
                                    if (Stream.this.remote_num >= Stream.this.last_num) {
                                        Stream.this.remote_killed = true;
                                    }
                                }
                                catch (Exception e) {
                                    Stream.this.sc.msg(e);
                                    Stream.this.sc.out_queue_commands.insertElementAt(Stream.this.sc.makeCommand(Stream.this.id, "KILL:"), 0);
                                }
                                Stream.this.sc.out_queue_commands.insertElementAt(Stream.this.sc.makeCommand(Stream.this.id, "A:M:" + Stream.this.remote_num), 0);
                            }
                        });
                        Thread.currentThread().setName("Tunnel3 stream " + this.id + " reader from " + this.sock);
                        this.sc.out_queues.put(String.valueOf(this.id), new Vector());
                        this.in = this.sock.getInputStream();
                        this.sock.setSoTimeout(1000);
                        int bytes_read = 0;
                        Vector q = this.sc.getQueue(String.valueOf(this.id));
                        Worker.startWorker(new Runnable(){

                            @Override
                            public void run() {
                                Thread.currentThread().setName("Tunnel3 buffer tuner " + Stream.this.id + " to " + Stream.this.sock);
                                while (Stream.this.sc.isActive() && !Stream.this.sock.isClosed() && Stream.this.buffer_tuner_status.length() == 0) {
                                    int byte_speed;
                                    if (Stream.this.ram_allocated_out < 0x4000000L && StreamController.total_ram_used < StreamController.ram_max_total && (byte_speed = Stream.this.sc.getSpeedAndReset(Stream.this.id)) > 524288 && (long)byte_speed > Stream.this.ram_allocated_out / 2L) {
                                        long new_bytes = Stream.this.ram_allocated_out;
                                        if (Stream.this.ram_allocated_out > 0xE00000L) {
                                            new_bytes = 0x4000000L - Stream.this.ram_allocated_out;
                                        }
                                        StreamController.addRamAllocated(new_bytes);
                                        Stream.this.ram_allocated_out += new_bytes;
                                        Stream.this.sc.msg("++++++++++++++Stream " + Stream.this.id + " max ram set to:" + Common.format_bytes_short(Stream.this.ram_allocated_out));
                                    }
                                    try {
                                        Thread.sleep(2000L);
                                    }
                                    catch (InterruptedException interruptedException) {
                                        // empty catch block
                                    }
                                }
                            }
                        });
                        Stream parent_st = null;
                        if (this.parent_stream_id > 0) {
                            parent_st = (Stream)this.sc.streams.get(String.valueOf(this.parent_stream_id));
                        }
                        while (bytes_read >= 0 && this.sc.isActive() && !this.sock.isClosed()) {
                            byte[] b = new byte[16384];
                            bytes_read = -1;
                            try {
                                bytes_read = this.in.read(b);
                                this.last_activity = System.currentTimeMillis();
                                if (parent_st != null) {
                                    parent_st.last_activity = this.last_activity;
                                }
                            }
                            catch (SocketTimeoutException e) {
                                bytes_read = 0;
                            }
                            if (bytes_read > 0) {
                                Chunk c = new Chunk(this.id, b, bytes_read, this.local_num++);
                                if (c.len > 0) {
                                    StreamController.addRam(this.id, c.len);
                                }
                                q.addElement(c);
                                int loops = 0;
                                while (StreamController.getRam(this.id) > this.ram_allocated_out && loops++ < 1000 && this.sc.isActive()) {
                                    if (loops == 100) {
                                        this.sc.msg("Slowing " + this.id + " transfer due to full buffer: q.size=" + q.size() + " ram_used_out=" + StreamController.getRam(this.id) + " ram_allocated_out=" + this.ram_allocated_out + " sc.last_cache_ram=" + this.sc.last_cache_ram);
                                    }
                                    Thread.sleep(1L);
                                }
                                loops = 0;
                                while (q.size() > 20000 && loops++ < 60 && this.sc.isActive()) {
                                    Thread.sleep(1000L);
                                }
                                if (loops >= 60) {
                                    this.sock.close();
                                }
                                if (StreamController.getRam(this.id) > this.ram_allocated_out && this.sc.last_cache_ram < 0x100000L) {
                                    this.sc.msg("RAM " + this.id + " buffer full:" + Common.format_bytes_short(StreamController.getRam(this.id)) + " of " + Common.format_bytes_short(this.ram_allocated_out) + " with last localCache of " + Common.format_bytes_short(this.sc.last_cache_ram));
                                }
                            }
                            if (System.currentTimeMillis() - this.last_activity <= 600000L) continue;
                            this.sock.close();
                            this.sc.msg("Closing idle stream..." + this.sock + ":" + Thread.currentThread().getName());
                        }
                        break block53;
                    }
                    catch (Exception e) {
                        if (!this.sock.isClosed()) {
                            this.sc.msg(e);
                        }
                        this.buffer_tuner_status.append("done");
                        if (!this.remote_killed) {
                            this.sc.out_queue_commands.addElement(this.sc.makeCommand(this.id, "END:" + this.local_num));
                        }
                        try {
                            int loops = 0;
                            while (this.remote_num < this.last_num || this.last_num < 0) {
                                Thread.sleep(100L);
                                if (loops++ > 100) break;
                            }
                            this.in.close();
                            this.sock.close();
                        }
                        catch (Exception e2) {
                            this.sc.msg(e2);
                        }
                    }
                    this.sc.in_queues.remove(String.valueOf(this.id));
                }
                catch (Throwable throwable) {
                    this.buffer_tuner_status.append("done");
                    if (!this.remote_killed) {
                        this.sc.out_queue_commands.addElement(this.sc.makeCommand(this.id, "END:" + this.local_num));
                    }
                    try {
                        int loops = 0;
                        while (this.remote_num < this.last_num || this.last_num < 0) {
                            Thread.sleep(100L);
                            if (loops++ > 100) break;
                        }
                        this.in.close();
                        this.sock.close();
                    }
                    catch (Exception e) {
                        this.sc.msg(e);
                    }
                }
                this.sc.streams.remove(String.valueOf(this.id));
                Vector q = null;
                Object object = this.sc.out_queue_remove;
                synchronized (object) {
                    q = (Vector)this.sc.out_queues.remove(String.valueOf(this.id));
                }
                if (q != null && !this.local_killed) {
                    this.sc.getQueue("unknown").addAll(q);
                } else if (q != null && this.local_killed) {
                    while (q.size() > 0) {
                        q.remove(0);
                    }
                }
                Enumeration<Object> keys = this.sc.localCache.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    Chunk c2 = (Chunk)this.sc.localCache.get(key);
                    if (c2 == null || c2.id != this.id) continue;
                    this.sc.localCache.remove(key);
                }
                break block54;
                this.sc.in_queues.remove(String.valueOf(this.id));
                this.sc.streams.remove(String.valueOf(this.id));
                q = null;
                keys = this.sc.out_queue_remove;
                synchronized (keys) {
                    q = (Vector)this.sc.out_queues.remove(String.valueOf(this.id));
                }
                if (q != null && !this.local_killed) {
                    this.sc.getQueue("unknown").addAll(q);
                } else if (q != null && this.local_killed) {
                    while (q.size() > 0) {
                        q.remove(0);
                    }
                }
                keys = this.sc.localCache.keys();
                while (keys.hasMoreElements()) {
                    String key = keys.nextElement().toString();
                    Chunk c2 = (Chunk)this.sc.localCache.get(key);
                    if (c2 == null || c2.id != this.id) continue;
                    this.sc.localCache.remove(key);
                }
                throw throwable;
            }
            this.buffer_tuner_status.append("done");
            if (!this.remote_killed) {
                this.sc.out_queue_commands.addElement(this.sc.makeCommand(this.id, "END:" + this.local_num));
            }
            try {
                int loops = 0;
                while (this.remote_num < this.last_num || this.last_num < 0) {
                    Thread.sleep(100L);
                    if (loops++ > 100) break;
                }
                this.in.close();
                this.sock.close();
            }
            catch (Exception e) {
                this.sc.msg(e);
            }
            this.sc.in_queues.remove(String.valueOf(this.id));
            this.sc.streams.remove(String.valueOf(this.id));
            Vector q = null;
            Enumeration<Object> keys = this.sc.out_queue_remove;
            synchronized (keys) {
                q = (Vector)this.sc.out_queues.remove(String.valueOf(this.id));
            }
            if (q != null && !this.local_killed) {
                this.sc.getQueue("unknown").addAll(q);
            } else if (q != null && this.local_killed) {
                while (q.size() > 0) {
                    q.remove(0);
                }
            }
            keys = this.sc.localCache.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                Chunk c2 = (Chunk)this.sc.localCache.get(key);
                if (c2 == null || c2.id != this.id) continue;
                this.sc.localCache.remove(key);
            }
        }
        StreamController.memory.remove(String.valueOf(this.id));
        this.sc.last_bytes_sent.remove(String.valueOf(this.id));
        this.sc.last_bytes_sent_time.remove(String.valueOf(this.id));
        StreamController.addRamAllocated(this.ram_allocated_out * -1L);
    }

    public void kill() throws IOException {
        this.local_killed = true;
        this.sock.close();
    }
}

