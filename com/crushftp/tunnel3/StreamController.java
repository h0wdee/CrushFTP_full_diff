/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel3;

import com.crushftp.client.Common;
import com.crushftp.client.File_S;
import com.crushftp.client.HttpURLConnection;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import com.crushftp.tunnel2.Chunk;
import com.crushftp.tunnel2.Tunnel2;
import com.crushftp.tunnel3.Stream;
import com.crushftp.tunnel3.StreamFTP;
import com.crushftp.tunnel3.StreamTuner;
import com.crushtunnel.gui.GUIFrame;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class StreamController
implements Runnable {
    public static String version = "3.4.2";
    public Properties tunnel = null;
    String auth = null;
    VRL vrl = null;
    Vector log = null;
    public Vector incoming = new Vector();
    public Vector outgoing = new Vector();
    boolean ready = false;
    public Properties in_queues = new Properties();
    public Properties bad_queues = new Properties();
    public Properties out_queues = new Properties();
    Enumeration out_queue_enum = this.out_queues.keys();
    public Vector out_queue_commands = new Vector();
    public Object out_queue_remove = new Object();
    StreamController sc = this;
    public static Object ram_lock2 = new Object();
    public static Object bytes_lock2 = new Object();
    public Properties streams = new Properties();
    public Properties localCache = new Properties();
    public Object command_num_lock = new Object();
    public int command_num = -1;
    public Properties closeRequests = new Properties();
    public StreamTuner st = new StreamTuner(this);
    public long last_send_activity = System.currentTimeMillis();
    public long last_receive_activity = System.currentTimeMillis();
    boolean allowReverseMode = false;
    boolean active = true;
    long last_cache_ram = 0L;
    String username = "";
    String password = "";
    String clientid = "CrushTunnel";
    public Properties stats = new Properties();
    public static Properties memory = new Properties();
    static long total_ram_used = 0L;
    public static long ram_max_total = 0x100000 * Integer.parseInt(System.getProperty("crushftp.tunnel_ram_cache", "128"));
    Properties last_bytes_sent = new Properties();
    Properties last_bytes_sent_time = new Properties();
    public static long last_version_check = 0L;
    public static Object version_check_lock = new Object();
    public static String old_msg = null;
    boolean reset_wanted = false;
    Properties out_binds = new Properties();
    Properties in_binds = new Properties();
    public Object bind_lock = new Object();
    RandomAccessFile tunnel_log = null;
    public Object log_lock = new Object();
    static int last_day = 0;
    static long last_time = System.currentTimeMillis();
    static SimpleDateFormat dd = new SimpleDateFormat("dd");

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long addRam(int id, long amount) {
        Object object = ram_lock2;
        synchronized (object) {
            memory.put(String.valueOf(id), String.valueOf(amount += Long.parseLong(memory.getProperty(String.valueOf(id), "0"))));
            return amount;
        }
    }

    public static long getRam(int id) {
        return Long.parseLong(memory.getProperty(String.valueOf(id), "0"));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void addRamAllocated(long amount) {
        Object object = ram_lock2;
        synchronized (object) {
            total_ram_used += amount;
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("C " + version + " Initialized. " + Common.format_bytes_short(ram_max_total));
            Properties p = new Properties();
            int x = 0;
            while (x < args.length) {
                String[] s = args[x].split(";");
                int xx = 0;
                while (xx < s.length) {
                    String key = s[xx].split("=")[0].trim();
                    String val = "";
                    try {
                        val = s[xx].split("=")[1].trim();
                    }
                    catch (Exception exception) {}
                    while (key.startsWith("-")) {
                        key = key.substring(1);
                    }
                    p.put(key.toUpperCase(), val);
                    ++xx;
                }
                ++x;
            }
            if (p.getProperty("DUMPMD5", "").equals("true")) {
                Tunnel2.dumpMD5s(p);
                return;
            }
            if (new File(p.getProperty("PASSWORD")).exists() && new File(p.getProperty("PASSWORD")).length() < 100L) {
                RandomAccessFile in = new RandomAccessFile(new File_S(p.getProperty("PASSWORD")), "r");
                byte[] b = new byte[(int)in.length()];
                in.readFully(b);
                in.close();
                p.put("PASSWORD", new String(b, "UTF8").trim());
            }
            Common.trustEverything();
            StreamController sc = new StreamController(String.valueOf(p.getProperty("PROTOCOL")) + "://" + p.getProperty("HOST") + ":" + p.getProperty("PORT") + "/", p.getProperty("USERNAME"), p.getProperty("PASSWORD"), null);
            sc.startThreads();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setLog(Vector log2) {
        this.log = log2;
    }

    public void startThreads() throws Exception {
        if (this.auth == null) {
            this.auth = Common.login(this.vrl.toString(), this.username, this.password, this.clientid);
        }
        if (this.tunnel == null) {
            this.sc.setTunnel(this.getTunnelItem(null));
        }
        if (this.tunnel == null) {
            throw new Exception("Can't start tunnel, server returned blank tunnel configuration:" + this.tunnel);
        }
        Worker.startWorker(this);
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                StreamController.this.sc.startSocket();
            }
        });
        int x = 0;
        while (x < 100) {
            if (this.ready) break;
            Thread.sleep(100L);
            ++x;
        }
        Thread.sleep(100L);
    }

    public void startReverseThreads() throws Exception {
        this.allowReverseMode = true;
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                StreamController.this.sc.startSocket();
            }
        });
        int x = 0;
        while (x < 100) {
            Thread.sleep(110L);
            if (this.ready) break;
            ++x;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public StreamController(Properties tunnel) {
        this.tunnel = tunnel;
        Properties properties = this.out_queues;
        synchronized (properties) {
            if (!this.out_queues.containsKey("unknown")) {
                this.out_queues.put("unknown", new Vector());
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public StreamController(String url, String username, String password, String clientid) {
        Properties properties = this.out_queues;
        synchronized (properties) {
            if (!this.out_queues.containsKey("unknown")) {
                this.out_queues.put("unknown", new Vector());
            }
        }
        this.vrl = new VRL(url);
        this.username = username;
        this.password = password;
        this.clientid = clientid;
    }

    public void setTunnel(Properties tunnel) {
        this.tunnel = tunnel;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    @Override
    public void run() {
        try {
            Worker.startWorker(this.st);
            this.startStopTunnel(true);
            this.ready = true;
            while (this.isActive()) {
                try {
                    this.checkAckLoop();
                }
                catch (NullPointerException nullPointerException) {
                    // empty catch block
                }
            }
        }
        catch (Exception e) {
            this.msg(e);
        }
    }

    public void reset() {
        try {
            this.startStopTunnel(false);
        }
        catch (Exception e) {
            this.msg(e);
        }
        try {
            while (this.st.isActive()) {
                Thread.sleep(100L);
            }
            this.auth = null;
            this.tunnel = null;
            this.active = true;
            int x = 0;
            while (x < Integer.parseInt(System.getProperty("crushtunnel.reset_retries", "345600"))) {
                try {
                    this.startThreads();
                }
                catch (Exception e) {
                    this.msg(e);
                    Thread.sleep(1000L);
                    ++x;
                }
            }
        }
        catch (Exception e) {
            this.msg(e);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void addBytes(int id, int bytes) {
        Object object = bytes_lock2;
        synchronized (object) {
            long l = Long.parseLong(this.last_bytes_sent.getProperty(String.valueOf(id), "0"));
            this.last_bytes_sent.put(String.valueOf(id), String.valueOf(l += (long)bytes));
            if (!this.last_bytes_sent_time.containsKey(String.valueOf(id))) {
                this.last_bytes_sent_time.put(String.valueOf(id), String.valueOf(System.currentTimeMillis()));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getSpeedAndReset(int id) {
        Object object = bytes_lock2;
        synchronized (object) {
            long secs = System.currentTimeMillis() - Long.parseLong(this.last_bytes_sent_time.getProperty(String.valueOf(id), String.valueOf(System.currentTimeMillis())));
            this.last_bytes_sent_time.put(String.valueOf(id), String.valueOf(System.currentTimeMillis()));
            if ((secs /= 1000L) == 0L) {
                secs = 1L;
            }
            long speed = Long.parseLong(this.last_bytes_sent.getProperty(String.valueOf(id), "0")) / secs;
            this.last_bytes_sent.put(String.valueOf(id), "0");
            return (int)speed;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void checkAckLoop() throws Exception {
        Thread.currentThread().setName("Tunnel3 ACK Thread:" + this.localCache.size());
        if (this.reset_wanted) {
            this.reset_wanted = false;
            this.reset();
            this.reset_wanted = false;
        }
        int loop_delay = 1000;
        int timeout = Integer.parseInt(this.tunnel.getProperty("ackTimeout", "30")) * 1000;
        Thread.sleep(loop_delay);
        Enumeration<Object> keys = this.localCache.keys();
        int min_num = 0;
        int max_num = 0;
        Properties priority_list_data = new Properties();
        Vector<Chunk> pending_commands = new Vector<Chunk>();
        long ram_used = 0L;
        int normal_chunks = 0;
        int command_chunks = 0;
        Properties writers = new Properties();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            Chunk c = (Chunk)this.localCache.get(key);
            if (c.len > 0) {
                ram_used += (long)c.len;
            }
            if (c.isCommand()) {
                ++command_chunks;
            } else {
                ++normal_chunks;
            }
            if (c.sw != null && c.sw.urlc != null) {
                String s = (String)writers.get(c.sw.urlc.getBindIp());
                if (s == null) {
                    s = "0";
                }
                int count = Integer.parseInt(s);
                writers.put(c.sw.urlc.getBindIp(), String.valueOf(++count));
            }
            if (c == null || System.currentTimeMillis() - c.time <= (long)timeout) continue;
            this.msg("#####################################################NO ACK after " + timeout + "ms, RESENDING:" + c + "#####################################################");
            if (c.num < 0) {
                if (c.getCommand().startsWith("PING")) {
                    this.localCache.remove(key);
                    continue;
                }
                if (c.getCommand().startsWith("VERSION")) {
                    this.localCache.remove(key);
                    continue;
                }
                pending_commands.addElement(c);
                continue;
            }
            if (c.num < min_num) {
                min_num = c.num;
            }
            if (c.num > max_num) {
                max_num = c.num;
            }
            priority_list_data.put(String.valueOf(c.num), c);
        }
        Vector v = this.getQueue("unknown");
        while (v.size() > 1000) {
            v.remove(0);
        }
        this.last_cache_ram = ram_used;
        this.msg("Ram currently used:" + Common.format_bytes_short(ram_used) + " for " + command_chunks + " commands and " + normal_chunks + " data chunks. Writers:" + writers + " in:" + this.sc.incoming.size() + " out:" + this.sc.outgoing.size());
        int x = max_num;
        while (x >= min_num) {
            Chunk c = (Chunk)priority_list_data.get(String.valueOf(x));
            if (c != null) {
                c.time = System.currentTimeMillis();
                this.getQueue(String.valueOf(c.id)).insertElementAt(c, 0);
            }
            --x;
        }
        while (pending_commands.size() > 0) {
            Chunk c = (Chunk)pending_commands.remove(0);
            c.time = System.currentTimeMillis();
            this.sc.out_queue_commands.insertElementAt(c, 0);
        }
        Object object = version_check_lock;
        synchronized (object) {
            if (System.currentTimeMillis() - last_version_check > 60000L) {
                last_version_check = System.currentTimeMillis();
                this.sc.out_queue_commands.addElement(this.sc.makeCommand(0, "VERSION_CHECK:" + version));
            }
        }
    }

    public Vector getQueue(String id) {
        Vector q = (Vector)this.out_queues.get(String.valueOf(id));
        if (q == null) {
            q = (Vector)this.out_queues.get("unknown");
        }
        return q;
    }

    public int getQueueCount() {
        int total = 0;
        Enumeration<Object> keys = this.out_queues.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            Vector q = (Vector)this.out_queues.get(key);
            total += q.size();
        }
        return total;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Chunk popOut() {
        int enum_loops = 0;
        Chunk c = null;
        Object object = this.out_queue_remove;
        synchronized (object) {
            block3: while (c == null && enum_loops++ < 2) {
                if (!this.out_queue_enum.hasMoreElements()) {
                    this.out_queue_enum = this.out_queues.keys();
                }
                while (c == null) {
                    if (!this.out_queue_enum.hasMoreElements()) continue block3;
                    String key = this.out_queue_enum.nextElement().toString();
                    Vector q = (Vector)this.out_queues.get(key);
                    if (q == null || q.size() <= 0) continue;
                    c = (Chunk)q.remove(0);
                }
            }
        }
        return c;
    }

    public void startServerTunnel() throws IOException {
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                try {
                    while (StreamController.this.isActive()) {
                        StreamController.this.checkAckLoop();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public HttpURLConnection addTransport(boolean write, String channel_id) {
        HttpURLConnection urlc;
        block12: {
            try {
                String bind_ip = null;
                VRL vrl_tmp = this.vrl;
                if (!System.getProperty("crushftp.bind_ips", "").equals("")) {
                    String ips = System.getProperty("crushftp.bind_ips", "");
                    String ip_load_write = System.getProperty("crushftp.bind_ips_out", "");
                    String ip_load_read = System.getProperty("crushftp.bind_ips_in", "");
                    Object object = this.bind_lock;
                    synchronized (object) {
                        String lowest_ip = "0.0.0.0:100";
                        Properties load = new Properties();
                        int x = 0;
                        while (x < ips.split(",").length) {
                            String ip = ips.split(",")[x].trim();
                            float l = 0.0f;
                            if (x < (write ? ip_load_write : ip_load_read).split(",").length) {
                                l = Integer.parseInt((write ? ip_load_write : ip_load_read).split(",")[x].trim());
                            }
                            float count = Integer.parseInt((write ? this.out_binds : this.in_binds).getProperty(ip, "0"));
                            float ip_load = count / l;
                            load.put(ip, String.valueOf(ip_load));
                            if (ip_load < Float.parseFloat(lowest_ip.split(":")[1])) {
                                lowest_ip = String.valueOf(ip) + ":" + ip_load;
                            }
                            ++x;
                        }
                        bind_ip = lowest_ip.split(":")[0];
                        (write ? this.out_binds : this.in_binds).put(lowest_ip.split(":")[0], String.valueOf(Integer.parseInt((write ? this.out_binds : this.in_binds).getProperty(bind_ip, "0")) + 1));
                        vrl_tmp = new VRL(Common.replace_str("" + this.vrl, this.vrl.getHost(), String.valueOf(bind_ip) + "~" + this.vrl.getHost()));
                    }
                    this.msg("in:" + this.in_binds + "out:" + this.out_binds);
                }
                VRL u = new VRL(vrl_tmp + "CRUSH_STREAMING_HTTP_PROXY3/?writing=" + write + "&channel_id=" + channel_id + "&tunnelId=" + this.tunnel.getProperty("id"));
                urlc = (HttpURLConnection)u.openConnection();
                urlc.setBindIp(bind_ip);
                urlc.setRequestMethod("POST");
                urlc.setRequestProperty("Cookie", "CrushAuth=" + this.auth + ";");
                urlc.setUseCaches(false);
                urlc.setDoOutput(write);
                if (write) {
                    urlc.setUseChunkedStreaming(true);
                }
                if (write || urlc.getResponseCode() != 302) break block12;
                this.msg("CrushAuth token logged out of server, resetting tunnel to start over.");
                urlc.disconnect();
                if (!this.reset_wanted) {
                    this.reset_wanted = true;
                }
                return null;
            }
            catch (Exception e) {
                this.msg(e);
                return null;
            }
        }
        this.msg("Tunnel3:getSendGet:urlc:" + urlc);
        return urlc;
    }

    public void startSocket() {
        block60: {
            String bindip = this.tunnel.getProperty("bindIp", "0.0.0.0");
            int bindport = Integer.parseInt(this.tunnel.getProperty("localPort", "0"));
            ServerSocket ss1 = null;
            ServerSocket ss2 = null;
            Thread.currentThread().setName("Tunnel:ConnectionHandler:bindip:" + bindip + ":" + bindport);
            try {
                try {
                    if (this.allowReverseMode) {
                        this.ready = true;
                    }
                    while (!this.ready) {
                        Thread.sleep(100L);
                    }
                    if (this.tunnel.getProperty("reverse", "false").equals("true") && !this.allowReverseMode) {
                        this.startStopTunnel(true);
                    }
                    ServerSocket ss0 = null;
                    int sock_num = 0;
                    while (this.isActive()) {
                        if (this.tunnel.getProperty("reverse", "false").equals("false") || this.allowReverseMode) {
                            try {
                                if (ss1 == null) {
                                    ss1 = new ServerSocket(bindport, 1000, InetAddress.getByName(bindip));
                                    this.msg("Tunnel3:ConnectionHandler:bound port:" + bindport);
                                    ss1.setSoTimeout(100);
                                    if (!this.allowReverseMode) {
                                        this.startStopTunnel(true);
                                    }
                                    this.msg("Tunnel3:ConnectionHandler:tunnel started.");
                                }
                                if (String.valueOf(bindport).startsWith("444") && ss2 == null) {
                                    ss2 = new ServerSocket(bindport + 10, 1000, InetAddress.getByName(bindip));
                                    this.msg("Tunnel3:ConnectionHandler:bound port:" + (bindport + 10));
                                    ss2.setSoTimeout(100);
                                }
                                if (++sock_num == 1) {
                                    ss0 = ss1;
                                } else {
                                    ss0 = ss2;
                                    sock_num = 0;
                                }
                                if (ss0 == null) {
                                    ss0 = ss1;
                                }
                                Socket proxy = ss0.accept();
                                this.msg("Tunnel3:ConnectionHandler:received connection:" + proxy);
                                Socket control = proxy;
                                boolean ftp = String.valueOf(bindport).endsWith("21");
                                try {
                                    if (String.valueOf(bindport).startsWith("444")) {
                                        if (ss0.getLocalPort() == ss2.getLocalPort()) {
                                            ftp = true;
                                        }
                                        if (ftp) {
                                            this.tunnel.put("destPort", "55521");
                                        } else {
                                            this.tunnel.put("destPort", "55580");
                                        }
                                    } else if (bindport == 55555 || this.tunnel.getProperty("destPort").equals("55555") || this.tunnel.getProperty("destPort").equals("55580") || this.tunnel.getProperty("destPort").equals("55521") || this.tunnel.getProperty("destPort").equals("0")) {
                                        ftp = true;
                                        int x = 0;
                                        while (x < 50 && ftp) {
                                            if (proxy.getInputStream().available() > 0) {
                                                ftp = false;
                                            }
                                            if (ftp) {
                                                Thread.sleep(10L);
                                            }
                                            ++x;
                                        }
                                        if (ftp) {
                                            this.tunnel.put("destPort", "55521");
                                        } else {
                                            this.tunnel.put("destPort", "55580");
                                        }
                                    }
                                }
                                catch (Exception e) {
                                    this.msg(e);
                                    try {
                                        proxy.close();
                                    }
                                    catch (Exception ee) {
                                        Common.log("TUNNEL", 1, ee);
                                    }
                                }
                                this.msg("Tunnel3:ConnectionHandler:ftp=" + ftp);
                                if (ftp) {
                                    ServerSocket ssProxyControl = new ServerSocket(0);
                                    int localPort = ssProxyControl.getLocalPort();
                                    control = new Socket("127.0.0.1", localPort);
                                    int stream_id = this.process(ssProxyControl.accept(), this.tunnel.getProperty("destIp"), Integer.parseInt(this.tunnel.getProperty("destPort")), 0, 0);
                                    ssProxyControl.close();
                                    StreamFTP ftpp = new StreamFTP(this.sc, stream_id);
                                    ftpp.proxyNATs(control, proxy);
                                    this.msg("Tunnel3:Started FTP NAT:control=" + control + " proxy=" + proxy);
                                } else {
                                    this.process(proxy, this.tunnel.getProperty("destIp"), Integer.parseInt(this.tunnel.getProperty("destPort")), 0, 0);
                                }
                            }
                            catch (SocketTimeoutException proxy) {
                            }
                            catch (Exception e) {
                                this.msg(e);
                                try {
                                    Thread.sleep(1000L);
                                }
                                catch (Exception ee) {
                                    Common.log("TUNNEL", 1, ee);
                                }
                            }
                            if (!this.allowReverseMode || System.currentTimeMillis() - this.last_receive_activity <= 30000L) continue;
                            this.msg("Tunnel is apparently inactive..." + (System.currentTimeMillis() - this.last_receive_activity));
                            this.startStopTunnel(false);
                            continue;
                        }
                        Thread.sleep(1000L);
                    }
                    if (ss1 != null) {
                        ss1.close();
                    }
                    if (ss2 != null) {
                        ss2.close();
                    }
                }
                catch (Exception e) {
                    Common.log("TUNNEL", 1, e);
                    try {
                        if (ss1 != null) {
                            ss1.close();
                        }
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                    try {
                        if (ss2 != null) {
                            ss2.close();
                        }
                        break block60;
                    }
                    catch (Exception exception) {}
                    break block60;
                }
            }
            catch (Throwable throwable) {
                try {
                    if (ss1 != null) {
                        ss1.close();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                try {
                    if (ss2 != null) {
                        ss2.close();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                throw throwable;
            }
            try {
                if (ss1 != null) {
                    ss1.close();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                if (ss2 != null) {
                    ss2.close();
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        try {
            this.startStopTunnel(false);
        }
        catch (Exception e) {
            Common.log("TUNNEL", 1, e);
        }
    }

    public boolean processCommand(final Chunk c, Stream st) {
        final String command = c.getCommand();
        if (!command.startsWith("PING")) {
            this.last_receive_activity = System.currentTimeMillis();
        }
        if (command.startsWith("A:")) {
            if (command.startsWith("A:M:")) {
                Enumeration<Object> keys = this.localCache.keys();
                while (keys.hasMoreElements()) {
                    Chunk c2;
                    String key = keys.nextElement().toString();
                    if (!key.startsWith(String.valueOf(c.id) + ":") || Integer.parseInt(key.split(":")[1]) >= Integer.parseInt(command.split(":")[2]) || (c2 = (Chunk)this.localCache.remove(key)) == null) continue;
                    StreamController.addRam(c2.id, c2.len * -1);
                }
            } else {
                Chunk c2 = (Chunk)this.localCache.remove(String.valueOf(c.id) + ":" + command.split(":")[1]);
                if (c2 != null) {
                    StreamController.addRam(c2.id, c2.len * -1);
                    if (c2.getCommand().startsWith("PINGSEND:")) {
                        this.msg("Latency one way:" + (System.currentTimeMillis() - Long.parseLong(c2.getCommand().split(":")[1])) + "ms");
                    }
                }
            }
            return false;
        }
        if (!c.getCommand().startsWith("PING")) {
            this.msg("RECEIVED COMMAND:" + c);
        }
        boolean close_stream = false;
        if (command.startsWith("END:")) {
            if (st != null) {
                st.last_num = Integer.parseInt(command.split(":")[1]);
            }
        } else if (command.startsWith("KILL:")) {
            try {
                if (st != null) {
                    st.kill();
                }
            }
            catch (IOException e) {
                this.msg(e);
            }
        } else if (command.startsWith("RESET:")) {
            this.reset_wanted = true;
        } else if (command.startsWith("CLOSE:")) {
            close_stream = true;
        } else if (command.startsWith("VERSION_OLD:")) {
            old_msg = "****************Tunnel client is too old.  You must update your client.  " + version + " less than " + command.split(":")[1] + "****************";
            if (GUIFrame.thisObj != null) {
                old_msg = "Tunnel client is too old.  You must restart your client.  " + version + " less than " + command.split(":")[1];
                GUIFrame.thisObj.showMessage(old_msg);
                try {
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            while (true) {
                                try {
                                    Thread.sleep(20000L);
                                }
                                catch (InterruptedException interruptedException) {
                                    // empty catch block
                                }
                                GUIFrame.thisObj.showMessage(old_msg);
                            }
                        }
                    });
                }
                catch (Exception e) {
                    this.msg(e);
                }
            } else {
                try {
                    Worker.startWorker(new Runnable(){

                        @Override
                        public void run() {
                            int x = 0;
                            while (x < 100) {
                                StreamController.this.msg(old_msg);
                                try {
                                    Thread.sleep(1000L);
                                }
                                catch (InterruptedException interruptedException) {
                                    // empty catch block
                                }
                                ++x;
                            }
                        }
                    });
                }
                catch (Exception e) {
                    this.msg(e);
                }
            }
            try {
                this.sc.startStopTunnel(false);
            }
            catch (Exception e) {
                this.msg(e);
            }
        } else if (command.startsWith("CONNECT:")) {
            try {
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            Socket sock = new Socket(command.split(":")[1], Integer.parseInt(command.split(":")[2]));
                            Properties queue = new Properties();
                            StreamController.this.in_queues.put(String.valueOf(c.id), queue);
                            int parent_stream_id = 0;
                            if (command.split(":").length > 3) {
                                parent_stream_id = Integer.parseInt(command.split(":")[3]);
                            }
                            Stream st = new Stream(sock, c.id, StreamController.this.sc, StreamController.this.tunnel, parent_stream_id);
                            StreamController.this.sc.streams.put(String.valueOf(c.id), st);
                            Worker.startWorker(st);
                        }
                        catch (Exception e) {
                            StreamController.this.msg(e);
                        }
                    }
                });
            }
            catch (IOException e) {
                this.msg(e);
            }
        }
        this.out_queue_commands.addElement(this.sc.makeCommand(c.id, "A:" + c.num));
        return close_stream;
    }

    public int process(Socket sock, String host, int port, int id2, int parent_stream_id) throws Exception {
        if (id2 == 0) {
            id2 = (int)(Math.random() * 100000.0);
        }
        this.msg("Stream id is:" + id2);
        Properties queue = new Properties();
        this.sc.in_queues.put(String.valueOf(id2), queue);
        this.out_queue_commands.addElement(this.sc.makeCommand(id2, "CONNECT:" + host + ":" + port + ":" + parent_stream_id));
        Stream st = new Stream(sock, id2, this.sc, this.tunnel, parent_stream_id);
        this.streams.put(String.valueOf(id2), st);
        Worker.startWorker(st);
        return id2;
    }

    public String startStopTunnel(boolean start) throws Exception {
        if (start) {
            HttpURLConnection urlc = (HttpURLConnection)this.vrl.openConnection();
            urlc.setRequestMethod("POST");
            urlc.setRequestProperty("Cookie", "CrushAuth=" + this.auth + ";");
            urlc.setUseCaches(false);
            urlc.setDoOutput(true);
            String extra = "";
            if (this.tunnel.getProperty("configurable", "false").equals("true")) {
                extra = String.valueOf(extra) + "&bindIp=" + this.tunnel.getProperty("bindIp");
                extra = String.valueOf(extra) + "&localPort=" + this.tunnel.getProperty("localPort");
                extra = String.valueOf(extra) + "&destIp=" + this.tunnel.getProperty("destIp");
                extra = String.valueOf(extra) + "&destPort=" + this.tunnel.getProperty("destPort");
                extra = String.valueOf(extra) + "&channelsOutMax=" + this.tunnel.getProperty("channelsOutMax");
                extra = String.valueOf(extra) + "&channelsInMax=" + this.tunnel.getProperty("channelsInMax");
                extra = String.valueOf(extra) + "&reverse=" + this.tunnel.getProperty("reverse");
            }
            urlc.getOutputStream().write(("c2f=" + this.auth.toString().substring(this.auth.toString().length() - 4) + "&command=" + (start ? "startTunnel3" : "stopTunnel3") + "&tunnelId=" + this.tunnel.getProperty("id") + (this.clientid != null ? "&clientid=" + this.clientid : "") + extra).getBytes("UTF8"));
            urlc.getResponseCode();
            Common.consumeResponse(urlc.getInputStream());
            urlc.disconnect();
            this.msg("Tunnel3:Started tunnel." + start);
            return "success";
        }
        this.msg("Tunnel3:Closing tunnel:" + this.auth);
        this.active = false;
        int x = 0;
        while (x < 100) {
            Thread.sleep(100L);
            ++x;
        }
        return "success";
    }

    public boolean isActive() {
        return this.active;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void msg(String s) {
        if (!System.getProperty("crushftp.home", "").equals("")) {
            Common.log("TUNNEL", 0, s);
        } else if (this.log != null) {
            this.log.addElement(new Date() + ": " + s);
        } else {
            if (!System.getProperty("crushtunnel.log", "").equals("")) {
                Object object = this.log_lock;
                synchronized (object) {
                    if (this.tunnel_log == null) {
                        try {
                            new File(Common.all_but_last(System.getProperty("crushtunnel.log", ""))).mkdirs();
                            this.tunnel_log = new RandomAccessFile(System.getProperty("crushtunnel.log", ""), "rw");
                            this.tunnel_log.seek(this.tunnel_log.length());
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        this.tunnel_log.write((new Date() + ": " + s + "\r\n").getBytes());
                        if (last_day == 0) {
                            last_day = Calendar.getInstance().get(5);
                        }
                        if (Calendar.getInstance().get(5) != last_day) {
                            String f1_name;
                            this.tunnel_log.close();
                            this.tunnel_log = null;
                            File f1 = new File(System.getProperty("crushtunnel.log", ""));
                            String f2_name = f1_name = f1.getName();
                            f2_name = String.valueOf(f2_name.substring(0, f2_name.lastIndexOf("."))) + "_" + new SimpleDateFormat("ddMMyyyy").format(new Date(last_time)) + f2_name.substring(f2_name.lastIndexOf("."));
                            last_time = System.currentTimeMillis();
                            last_day = Calendar.getInstance().get(5);
                            f1.renameTo(new File(String.valueOf(Common.all_but_last(System.getProperty("crushtunnel.log", ""))) + f2_name));
                            long max_days = Long.parseLong(System.getProperty("crushtunnel.log.days", "30"));
                            long single_day = 86400000L;
                            File[] list = new File(Common.all_but_last(System.getProperty("crushtunnel.log", ""))).listFiles();
                            int x = 0;
                            while (list != null && x < list.length) {
                                if (list[x].getName().startsWith(f1_name.substring(0, f1_name.lastIndexOf("."))) && list[x].lastModified() < System.currentTimeMillis() - single_day * max_days) {
                                    list[x].delete();
                                }
                                ++x;
                            }
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            System.out.println(new Date() + ": " + s);
        }
    }

    public void msg(Exception e) {
        StackTraceElement[] ste = e.getStackTrace();
        this.msg(String.valueOf(Thread.currentThread().getName()) + ":" + e.toString());
        int x = 0;
        while (x < ste.length) {
            this.msg(String.valueOf(ste[x].getClassName()) + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber());
            ++x;
        }
    }

    public Properties getTunnelItem(String tunnel_name) throws IOException {
        this.msg("Tunnel3:Getting tunnel from server.");
        HttpURLConnection urlc = (HttpURLConnection)this.vrl.openConnection();
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Cookie", "CrushAuth=" + this.auth + ";");
        urlc.setUseCaches(false);
        urlc.setDoOutput(true);
        urlc.getOutputStream().write(("c2f=" + this.auth.toString().substring(this.auth.toString().length() - 4) + "&command=getTunnels").getBytes("UTF8"));
        urlc.getResponseCode();
        InputStream in = urlc.getInputStream();
        String data = "";
        int bytesRead = 0;
        byte[] b = new byte[16384];
        while (bytesRead >= 0) {
            bytesRead = in.read(b);
            if (bytesRead <= 0) continue;
            data = String.valueOf(data) + new String(b, 0, bytesRead, "UTF8");
        }
        in.close();
        urlc.disconnect();
        Properties use_tunnel = null;
        if (data.indexOf("<response>") > 0) {
            data = data.substring(data.indexOf("<response>") + "<response>".length(), data.indexOf("</response"));
            String[] tunnelsStr = Common.url_decode(data.replace('~', '%')).split(";;;");
            int x = 0;
            while (x < tunnelsStr.length) {
                Properties tunnel2 = new Properties();
                try {
                    tunnel2.load(new ByteArrayInputStream(tunnelsStr[x].getBytes("UTF8")));
                    if (tunnel_name != null) {
                        if (tunnel_name.equalsIgnoreCase(tunnel2.getProperty("name"))) {
                            use_tunnel = tunnel2;
                            break;
                        }
                    } else {
                        use_tunnel = tunnel2;
                        if (tunnel2.getProperty("localPort", "0").equals(System.getProperty("crushtunnel.magicport", "55555"))) {
                            break;
                        }
                    }
                }
                catch (Exception e) {
                    this.msg(e);
                }
                ++x;
            }
        }
        if (use_tunnel != null && use_tunnel.size() > 0) {
            this.msg("Tunnel3:Got tunnel from server:" + use_tunnel.size());
        } else {
            this.msg("**********NO TUNNEL FOUND FOR USER**********");
        }
        return use_tunnel;
    }

    public void updateStats(Chunk c, String channel_id, Vector history, String label, int index) {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Chunk makeCommand(int id, String command) {
        try {
            int num = 0;
            Object object = this.command_num_lock;
            synchronized (object) {
                num = this.command_num--;
                if (this.command_num < -1073741824) {
                    this.command_num = -1;
                }
            }
            return new Chunk(id, command.getBytes("UTF8"), command.length(), num);
        }
        catch (UnsupportedEncodingException unsupportedEncodingException) {
            return null;
        }
    }
}

