/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel2;

import com.crushftp.client.Base64;
import com.crushftp.client.Common;
import com.crushftp.client.VRL;
import com.crushftp.tunnel.Applet;
import com.crushftp.tunnel.Downloader;
import com.crushftp.tunnel.Uploader;
import com.crushftp.tunnel2.Chunk;
import com.crushftp.tunnel2.ConnectionHandler;
import com.crushftp.tunnel2.ConnectionOptimizer;
import com.crushftp.tunnel2.DProperties;
import com.crushftp.tunnel2.DVector;
import com.crushftp.tunnel2.Queue;
import com.crushftp.tunnel3.StreamController;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class Tunnel2 {
    public static final String version = "3.1.16";
    static Properties tunnels = new Properties();
    protected static long ramUsage = 0L;
    public static final Object ramLock = new Object();
    public static long maxRam = 0x100000 * Integer.parseInt(System.getProperty("crushftp.tunnel_ram_cache", "32"));
    int lastUrlIndex = 0;
    String url = null;
    String username = null;
    String password = null;
    String auth = null;
    public Properties tunnel = null;
    private Properties queues = new Properties();
    private Properties oldQueues = new Properties();
    private Vector oldQueueOrder = new Vector();
    private Properties remotes = new Properties();
    private DVector local = null;
    ConnectionOptimizer co = null;
    ConnectionHandler ch = null;
    int ping = -1;
    private boolean wantClose = false;
    boolean onlyOnce = false;
    public DProperties localAck = null;
    long lastAckCheck = System.currentTimeMillis();
    private boolean active = true;
    boolean isShutdown = false;
    private boolean ready = false;
    boolean allowReverseMode = false;
    long lastActivity = System.currentTimeMillis();
    private static Vector log = null;
    int commandNum = -1;
    long bytesIn = 0L;
    long bytesOut = 0L;
    ServerSocket localSocketProxy = null;
    Properties bindCounts = new Properties();
    float baseSpeedOut = 0.0f;
    float baseSpeedIn = 0.0f;

    public static void setMaxRam(int maxRam2) {
        maxRam = (long)maxRam2 * 1024L * 1024L;
        long maxRam3 = maxRam;
        if (maxRam != maxRam3) {
            Tunnel2.msg("Tunnel2:Using " + maxRam2 + " MB for cache on tunnels.");
        }
    }

    public static void main(String[] args) {
        try {
            System.out.println("C 3.1.16 Initialized. " + Common.format_bytes_short(maxRam));
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
                RandomAccessFile in = new RandomAccessFile(p.getProperty("PASSWORD"), "r");
                byte[] b = new byte[(int)in.length()];
                in.readFully(b);
                in.close();
                p.put("PASSWORD", new String(b, "UTF8").trim());
            }
            Common.trustEverything();
            Tunnel2 t = new Tunnel2(String.valueOf(p.getProperty("PROTOCOL")) + "://" + p.getProperty("HOST") + ":" + p.getProperty("PORT") + "/", p.getProperty("USERNAME"), p.getProperty("PASSWORD"), false);
            t.startThreads();
            if (t.tunnel.getProperty("tunnel_version", "tunnel2").equalsIgnoreCase("tunnel3")) {
                StreamController.main(args);
            }
        }
        catch (Exception e) {
            Tunnel2.msg(e);
        }
    }

    public static void dumpMD5s(final Properties p) {
        System.out.println("" + p);
        final Vector v = new Vector();
        final StringBuffer status = new StringBuffer();
        new Thread(new Runnable(){

            @Override
            public void run() {
                FileInputStream in = null;
                try {
                    try {
                        File f = new File(p.getProperty("PATH"));
                        in = new FileInputStream(f);
                        Tunnel2.getMd5s(in, true, p.getProperty("FORWARD", "true").equals("true"), f.length(), v, status, f.length());
                    }
                    catch (Exception e) {
                        Common.log("TUNNEL", 1, e);
                        try {
                            in.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        status.append("done");
                    }
                }
                finally {
                    try {
                        in.close();
                    }
                    catch (Exception exception) {}
                    status.append("done");
                }
            }
        }).start();
        while (status.length() == 0 || v.size() > 0) {
            if (v.size() > 0) {
                System.out.println(v.remove(0));
                continue;
            }
            try {
                Thread.sleep(100L);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void init() {
        this.ch = null;
        this.isShutdown = false;
        this.ping = -1;
        this.wantClose = false;
        this.onlyOnce = false;
        this.local = new DVector();
        this.localAck = new DProperties();
        this.lastAckCheck = System.currentTimeMillis();
        ramUsage = 0L;
        try {
            this.localSocketProxy = new ServerSocket(0);
        }
        catch (IOException e) {
            Tunnel2.msg(e);
        }
        this.setActive(true);
    }

    public static void setLog(Vector log2) {
        log = log2;
    }

    public void reset() {
        this.auth = null;
        this.tunnel = null;
        try {
            this.stopThisTunnel();
            Thread.sleep(1000L);
            this.init();
            this.startThreads();
        }
        catch (Exception e) {
            Tunnel2.msg(e);
        }
    }

    public void setAllowReverseMode(boolean allowReverseMode) {
        this.allowReverseMode = allowReverseMode;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void addRam(int len) {
        Object object = ramLock;
        synchronized (object) {
            ramUsage += (long)len;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void removeRam(int len) {
        Object object = ramLock;
        synchronized (object) {
            ramUsage -= (long)len;
        }
    }

    public synchronized void addBytesIn(long bytes) {
        this.bytesIn += bytes;
    }

    public synchronized void addBytesOut(long bytes) {
        this.bytesOut += bytes;
    }

    public long getBytesIn() {
        return this.bytesIn;
    }

    public long getBytesOut() {
        return this.bytesOut;
    }

    /*
     * Unable to fully structure code
     */
    public void startThreads() {
        while (true) {
            block11: {
                try {
                    this.checkStatus();
                    if (this.tunnel == null || this.tunnel.size() == 0 || this.tunnel.getProperty("tunnel_version", "tunnel2").equalsIgnoreCase("tunnel3")) break;
                    this.ch = new ConnectionHandler(this, this.tunnel.getProperty("bindIp", "0.0.0.0"), Integer.parseInt(this.tunnel.getProperty("localPort", "0")));
                    new Thread(this.ch).start();
                    break;
                }
                catch (Exception e) {
                    Tunnel2.msg(e);
                    if (this.username != null && !this.username.equals("")) break block11;
                    this.stopThisTunnel();
                    if (Applet.thisObj == null) break;
                    keys = Applet.thisObj.uploaders.keys();
                    ** while (keys.hasMoreElements())
                }
lbl-1000:
                // 1 sources

                {
                    key = keys.nextElement().toString();
                    u = (Uploader)Applet.thisObj.uploaders.get(key);
                    u.statusInfo.put("uploadStatus", "ERROR:" + e.getMessage());
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (Exception var5_7) {
                        // empty catch block
                    }
                    u.cancel();
                    continue;
                }
lbl27:
                // 1 sources

                keys = Applet.thisObj.downloaders.keys();
                while (keys.hasMoreElements()) {
                    key = keys.nextElement().toString();
                    d = (Downloader)Applet.thisObj.downloaders.get(key);
                    d.statusInfo.put("downloadStatus", "ERROR:" + e.getMessage());
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (Exception var5_9) {
                        // empty catch block
                    }
                    d.cancel();
                }
                break;
            }
            try {
                Tunnel2.msg("Server is not online, trying again in 10 seconds...");
                Thread.sleep(10000L);
            }
            catch (Exception var1_2) {}
        }
    }

    public static void msg(String s) {
        if (!System.getProperty("crushftp.home", "").equals("")) {
            Common.log("TUNNEL", 0, s);
        } else if (log != null) {
            log.addElement(new Date() + ": " + s);
        } else {
            System.out.println(new Date() + ": " + s);
        }
    }

    public static void msg(Exception e) {
        StackTraceElement[] ste = e.getStackTrace();
        Tunnel2.msg(String.valueOf(Thread.currentThread().getName()) + ":" + e.toString());
        int x = 0;
        while (x < ste.length) {
            Tunnel2.msg(String.valueOf(ste[x].getClassName()) + "." + ste[x].getMethodName() + ":" + ste[x].getLineNumber());
            ++x;
        }
    }

    public Tunnel2(String url, String username, String password, boolean onlyOnce) throws IOException {
        this.init();
        this.url = url;
        this.username = username;
        this.password = password;
        this.onlyOnce = onlyOnce;
        this.addQueue(new Queue(this, 0));
    }

    public Tunnel2(String crushAuth, Properties tunnel) throws IOException {
        this.init();
        this.tunnel = tunnel;
        tunnels.put(crushAuth, this);
        this.addQueue(new Queue(this, 0));
    }

    public void setTunnel(Properties tunnel) {
        this.tunnel = tunnel;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isShutdown() {
        return this.isShutdown;
    }

    public void setShutdown(boolean isShutdown) {
        this.isShutdown = isShutdown;
    }

    /*
     * Unable to fully structure code
     */
    public void waitForShutdown() throws InterruptedException {
        if (this.ch != null) ** GOTO lbl4
        return;
lbl-1000:
        // 1 sources

        {
            Thread.sleep(100L);
lbl4:
            // 2 sources

            ** while (this.ch.threads.size() > 0)
        }
lbl5:
        // 1 sources

    }

    public static Tunnel2 getTunnel(String crushAuth) {
        return (Tunnel2)tunnels.get(crushAuth);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void checkStatus() throws Exception {
        if (this.tunnel != null && this.getUrl(false) == null) {
            return;
        }
        Tunnel2 tunnel2 = this;
        synchronized (tunnel2) {
            if (this.auth == null) {
                if (this.username == null || this.username.equals("")) {
                    throw new Exception("Cannot restart tunnel using cookie token.");
                }
                this.auth = Common.login(this.getUrl(true), this.username, this.password, "CrushTunnel");
            }
            if (this.tunnel == null) {
                this.tunnel = this.getTunnel();
            }
        }
    }

    public Properties getTunnel() throws IOException {
        return this.getTunnelItem(null);
    }

    public Properties getTunnelItem(String tunnel_name) throws IOException {
        Tunnel2.msg("Tunnel2:Getting tunnel from server.");
        com.crushftp.client.HttpURLConnection urlc = (com.crushftp.client.HttpURLConnection)new VRL(this.getUrl(true)).openConnection();
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Cookie", "CrushAuth=" + this.auth + ";");
        urlc.setUseCaches(false);
        urlc.setDoOutput(true);
        urlc.getOutputStream().write(("c2f=" + this.auth.toString().substring(this.auth.toString().length() - 4) + "&command=getTunnels").getBytes("UTF8"));
        urlc.getResponseCode();
        InputStream in = urlc.getInputStream();
        String data = "";
        int bytesRead = 0;
        byte[] b = DProperties.getArray();
        while (bytesRead >= 0) {
            bytesRead = in.read(b);
            if (bytesRead <= 0) continue;
            data = String.valueOf(data) + new String(b, 0, bytesRead, "UTF8");
        }
        in.close();
        urlc.disconnect();
        b = DProperties.releaseArray(b);
        Properties use_tunnel = new Properties();
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
                    Tunnel2.msg(e);
                }
                ++x;
            }
        }
        if (use_tunnel != null && use_tunnel.containsKey("urls")) {
            this.url = use_tunnel.getProperty("urls");
        }
        if (use_tunnel != null && use_tunnel.size() > 0) {
            Tunnel2.msg("Tunnel2:Got tunnel from server:" + use_tunnel.size());
        } else {
            Tunnel2.msg("**********NO TUNNEL FOUND FOR USER**********");
        }
        return use_tunnel;
    }

    public void connect(int qid, String host, int port) throws Exception {
        Tunnel2.msg("Tunnel2:Connecting socket..." + host + ":" + port);
        this.ch = new ConnectionHandler(this);
        Socket sock = new Socket(host, port);
        this.ch.process(sock, host, port, false, false, qid);
    }

    public String stopThisTunnel() {
        Tunnel2.msg("Tunnel is stopping...locals=" + this.local.size() + ", remotes=" + this.remotes.size() + ", queues=" + this.queues.size() + ", threads=" + (this.ch != null ? this.ch.threads.size() : 0));
        this.ready = false;
        this.setActive(false);
        this.queues.clear();
        this.oldQueues.clear();
        this.oldQueueOrder.clear();
        try {
            this.localSocketProxy.close();
        }
        catch (IOException e) {
            Tunnel2.msg(e);
        }
        Enumeration<Object> keys = this.remotes.keys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement().toString();
            DProperties dp = (DProperties)this.remotes.remove(key);
            if (dp == null) continue;
            dp.close();
        }
        this.local.close();
        int loops = 0;
        while (this.ch != null && this.ch.threads.size() > 0 && loops++ < 20) {
            try {
                Thread.sleep(1000L);
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return "success";
    }

    public static String stopTunnel(String id) {
        Tunnel2 t = Tunnel2.getTunnel(id);
        if (t != null) {
            String msg = t.stopThisTunnel();
            if (msg.equals("success")) {
                tunnels.remove(id);
            }
            return msg;
        }
        return "not found";
    }

    public void startStopTunnel(boolean start) throws Exception {
        com.crushftp.client.HttpURLConnection urlc = (com.crushftp.client.HttpURLConnection)new VRL(this.getUrl(true)).openConnection();
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Cookie", "CrushAuth=" + this.auth + ";");
        urlc.setUseCaches(false);
        urlc.setDoOutput(true);
        urlc.getOutputStream().write(("c2f=" + this.auth.toString().substring(this.auth.toString().length() - 4) + "&command=" + (start ? "startTunnel2" : "stopTunnel2") + "&tunnelId=" + this.tunnel.getProperty("id")).getBytes("UTF8"));
        urlc.getResponseCode();
        Common.consumeResponse(urlc.getInputStream());
        urlc.disconnect();
        Tunnel2.msg("Tunnel2:Started tunnel." + start);
        if (this.co == null) {
            this.co = new ConnectionOptimizer(this);
            new Thread(this.co).start();
            Tunnel2.msg("Tunnel2:Started Optimizer.");
        }
    }

    public void markAvailable() {
        this.ready = true;
    }

    public boolean isReady() {
        return this.ready;
    }

    public void doConnect(int qid, String host, int port) throws IOException {
        this.writeLocal(this.makeCommand(0, "CONNECT:" + qid + ":" + host + ":" + port), 0);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public synchronized com.crushftp.client.HttpURLConnection getSendGet(final boolean write) throws Exception {
        Properties p;
        this.checkStatus();
        Tunnel2.msg("Tunnel2:getSendGet:write=" + write + " queues=" + this.getQueueCount());
        VRL u = null;
        if (this.bindCounts.size() == 0) {
            String[] bind_ips = System.getProperty("crushftp.bind_ips", "0.0.0.0").split(",");
            String[] bind_percentages_send = System.getProperty("crushftp.bind_percentages_send", "100").split(",");
            String[] bind_percentages_receive = System.getProperty("crushftp.bind_percentages_receive", "100").split(",");
            int x = 0;
            while (x < bind_ips.length) {
                p = new Properties();
                p.put("count_send", "0");
                p.put("count_receive", "0");
                p.put("percentage_send", "100");
                p.put("percentage_receive", "100");
                if (bind_percentages_send.length == bind_ips.length) {
                    p.put("percentage_send", bind_percentages_send[x]);
                }
                if (bind_percentages_receive.length == bind_ips.length) {
                    p.put("percentage_receive", bind_percentages_receive[x]);
                }
                this.bindCounts.put(bind_ips[x], p);
                ++x;
            }
        }
        if (!this.bindCounts.containsKey("0.0.0.0")) {
            Enumeration<Object> keys = this.bindCounts.keys();
            float low = 9999.0f;
            String lowest_ip = "";
            while (keys.hasMoreElements()) {
                String key = keys.nextElement().toString();
                p = (Properties)this.bindCounts.get(key);
                float f = Integer.parseInt(p.getProperty("count_" + (write ? "send" : "receive")));
                float val = Float.parseFloat(p.getProperty("percentage_" + (write ? "send" : "receive"), "100"));
                if (val == 0.0f) {
                    val = 1.0E-5f;
                }
                if (!((f /= val) < low)) continue;
                lowest_ip = key;
                low = f;
            }
            final String lowest_ip_f = lowest_ip;
            p = this.bindCounts;
            synchronized (p) {
                Properties p2 = (Properties)this.bindCounts.get(lowest_ip_f);
                int i = Integer.parseInt(p2.getProperty("count_" + (write ? "send" : "receive")));
                p2.put("count_" + (write ? "send" : "receive"), String.valueOf(i + 1));
            }
            VRL vrl = new VRL(this.getUrl(true));
            final Socket dsock = new Socket(vrl.getHost(), vrl.getPort(), InetAddress.getByName(lowest_ip_f), 0);
            Tunnel2.msg("***********************bind info:" + this.bindCounts);
            new Thread(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    Socket psock = null;
                    try {
                        psock = Tunnel2.this.localSocketProxy.accept();
                        Common.streamCopier(psock.getInputStream(), dsock.getOutputStream(), true, true, true);
                        Common.streamCopier(dsock.getInputStream(), psock.getOutputStream(), false, true, true);
                    }
                    catch (Exception e) {
                        try {
                            psock.close();
                            dsock.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        Tunnel2.msg(e);
                    }
                    Properties properties = Tunnel2.this.bindCounts;
                    synchronized (properties) {
                        Properties p = (Properties)Tunnel2.this.bindCounts.get(lowest_ip_f);
                        int i = Integer.parseInt(p.getProperty("count_" + (write ? "send" : "receive")));
                        if (i < 1) {
                            i = 1;
                        }
                        p.put("count_" + (write ? "send" : "receive"), String.valueOf(i - 1));
                    }
                }
            }).start();
            u = new VRL(String.valueOf(vrl.getProtocol()) + "://127.0.0.1:" + this.localSocketProxy.getLocalPort() + "/CRUSH_STREAMING_HTTP_PROXY2/?writing=" + write);
        } else {
            u = new VRL(String.valueOf(this.getUrl(true)) + "CRUSH_STREAMING_HTTP_PROXY2/?writing=" + write);
        }
        com.crushftp.client.HttpURLConnection urlc = (com.crushftp.client.HttpURLConnection)u.openConnection();
        urlc.setRequestMethod("POST");
        urlc.setRequestProperty("Cookie", "CrushAuth=" + this.auth + ";");
        urlc.setUseCaches(false);
        urlc.setDoOutput(write);
        if (write) {
            urlc.setChunkedStreamingMode(9999L);
        }
        Tunnel2.msg("Tunnel2:getSendGet:urlc:" + urlc);
        return urlc;
    }

    public void addRemote(int remoteId, DProperties remote) {
        this.remotes.put(String.valueOf(remoteId) + "_chunks", remote);
        Tunnel2.msg("Tunnel2:addRemote:" + remoteId);
    }

    public DProperties getRemote(int remoteId) {
        return (DProperties)this.remotes.get(String.valueOf(remoteId) + "_chunks");
    }

    public DVector getLocal() {
        return this.local;
    }

    public void releaseUrl(com.crushftp.client.HttpURLConnection urlc) {
        Tunnel2.msg("Tunnel2:releaseUrl:urlc:" + urlc);
        try {
            if (urlc != null) {
                urlc.disconnect();
            }
        }
        catch (IOException e) {
            Tunnel2.msg(e);
        }
    }

    public synchronized String getUrl(boolean toggle) {
        if (this.url == null) {
            return this.url;
        }
        if (toggle) {
            ++this.lastUrlIndex;
        }
        if (this.lastUrlIndex >= this.url.split(";").length) {
            this.lastUrlIndex = 0;
        }
        String s = this.url.split(";")[this.lastUrlIndex].trim();
        Tunnel2.msg("Returning URL:" + new VRL(s).safe());
        return s;
    }

    public String getAuth() {
        return this.auth;
    }

    public void addQueue(Queue q) throws IOException {
        Tunnel2.msg("Tunnel2:addQueue:qid:" + q.id);
        this.queues.put(String.valueOf(q.id), q);
        this.writeLocal(this.makeCommand(q.id, "PINGREADY:0"), 0);
    }

    public Queue getQueue(int qid) throws IOException {
        if (qid == 0 && !this.queues.containsKey("0")) {
            this.addQueue(new Queue(this, 0));
        }
        return (Queue)this.queues.get(String.valueOf(qid));
    }

    public Queue getOldQueue(int qid) {
        return (Queue)this.oldQueues.get(String.valueOf(qid));
    }

    public int getSends() {
        if (this.co == null || this.co.outgoing == null) {
            return 0;
        }
        return this.co.outgoing.size();
    }

    public int getGets() {
        if (this.co == null || this.co.incoming == null) {
            return 0;
        }
        return this.co.incoming.size();
    }

    public Queue removeQueue(int qid) {
        Queue q = (Queue)this.queues.remove(String.valueOf(qid));
        if (q != null) {
            this.oldQueues.put(String.valueOf(qid), q);
        }
        this.oldQueueOrder.addElement(String.valueOf(qid));
        while (this.oldQueueOrder.size() > 100) {
            String qid2 = this.oldQueueOrder.remove(0).toString();
            this.oldQueues.remove(qid2);
            DProperties dp = (DProperties)this.remotes.remove(String.valueOf(qid2) + "_chunks");
            if (dp == null) continue;
            dp.close();
        }
        return q;
    }

    public int getQueueCount() {
        return this.queues.size();
    }

    public synchronized void addWantClose() {
        this.wantClose = true;
    }

    public synchronized boolean getWantClose() {
        if (this.wantClose) {
            this.wantClose = false;
            return true;
        }
        return false;
    }

    public void setPing(int ping) {
        if (ping < this.ping || this.ping < 0) {
            Tunnel2.msg("Ping time: " + ping + "ms");
            this.ping = ping;
        }
        this.lastActivity = System.currentTimeMillis();
    }

    public int getPing() {
        return this.ping;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Chunk readLocal() throws IOException {
        DVector dVector = this.local;
        synchronized (dVector) {
            block4: {
                if (this.local.size() != 0) break block4;
                return null;
            }
            Chunk c = this.local.remove(0);
            return c;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeLocal(Chunk c, int i) throws IOException {
        DVector dVector = this.local;
        synchronized (dVector) {
            c.time = System.currentTimeMillis();
            if (i < 0) {
                this.local.addElement(c);
            } else {
                this.local.insertElementAt(c, i);
            }
        }
    }

    public boolean checkAcks() throws IOException {
        if (System.currentTimeMillis() - this.lastAckCheck < 10000L) {
            return true;
        }
        this.lastAckCheck = System.currentTimeMillis();
        long ackTimeout = Integer.parseInt(this.tunnel.getProperty("ackTimeout", "30")) * 1000;
        boolean ok = true;
        Enumeration keys = this.localAck.keys();
        while (keys.hasMoreElements() && this.active) {
            this.lastAckCheck = System.currentTimeMillis();
            String key = keys.nextElement().toString();
            Chunk c = this.localAck.get(key);
            if (c == null || System.currentTimeMillis() - c.time <= ackTimeout) continue;
            if (c.getCommand().startsWith("CONNECT")) {
                Tunnel2.msg("No Ack received for chunk:" + c.num + " after " + (System.currentTimeMillis() - c.time) + "ms. " + c.getCommand() + " localAck.size:" + this.localAck.size() + " Discarding...no further retries.");
                this.localAck.remove(key);
                continue;
            }
            Tunnel2.msg("No Ack received for chunk:" + c.num + " after " + (System.currentTimeMillis() - c.time) + "ms. " + c.getCommand() + " localAck.size:" + this.localAck.size());
            this.writeLocal(c, 0);
            ok = false;
        }
        this.lastAckCheck = System.currentTimeMillis();
        return ok;
    }

    public int getWaitingAckCount() {
        return this.localAck.size();
    }

    public static void getMd5s(InputStream bin, boolean chunked, boolean forward, long length, Vector md5s, StringBuffer status, long localSize) throws Exception {
        bin = new BufferedInputStream(bin);
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = 0;
        byte[] b = new byte[32768];
        int chunkSize = 0x100000;
        if (localSize < 0xA00000L && length < 0xA00000L) {
            chunkSize = 102400;
        }
        if (chunked && !forward && length > 0L) {
            int diff;
            int diff2 = diff = (int)(length - length / (long)chunkSize * (long)chunkSize);
            if (diff > 0) {
                while (diff > 0 && status.length() == 0) {
                    b = new byte[diff];
                    bytesRead = bin.read(b);
                    baos.write(b, 0, bytesRead);
                    diff -= bytesRead;
                }
                md5.update(baos.toByteArray());
                String hash = new BigInteger(1, md5.digest()).toString(16).toLowerCase();
                while (hash.length() < 32) {
                    hash = "0" + hash;
                }
                md5s.addElement("0-" + diff2 + ":" + hash);
                baos.reset();
                md5.reset();
            }
        }
        bytesRead = 0;
        b = new byte[32768];
        long loc = 0L;
        long lastLoc = 0L;
        while (bytesRead >= 0 && status.length() == 0) {
            if (baos.size() + b.length > chunkSize) {
                b = new byte[chunkSize - baos.size()];
            }
            if ((bytesRead = bin.read(b)) >= 0) {
                baos.write(b, 0, bytesRead);
                loc += (long)bytesRead;
            }
            if (baos.size() != chunkSize) continue;
            Tunnel2.updateMd5Data(md5, baos, chunked, md5s, lastLoc);
            lastLoc = loc;
            b = new byte[32768];
        }
        if (baos.size() > 0) {
            Tunnel2.updateMd5Data(md5, baos, chunked, md5s, lastLoc);
        }
        if (!chunked) {
            String hash = new BigInteger(1, md5.digest()).toString(16).toLowerCase();
            while (hash.length() < 32) {
                hash = "0" + hash;
            }
            md5s.addElement(hash);
        }
    }

    public static void updateMd5Data(MessageDigest md5, ByteArrayOutputStream baos, boolean chunked, Vector md5s, long loc) {
        md5.update(baos.toByteArray());
        long len = baos.toByteArray().length;
        baos.reset();
        if (chunked) {
            String hash = new BigInteger(1, md5.digest()).toString(16).toLowerCase();
            while (hash.length() < 32) {
                hash = "0" + hash;
            }
            md5s.addElement(String.valueOf(loc) + "-" + (loc + len) + ":" + hash);
            md5.reset();
        }
    }

    public static void getRemoteMd5s(String url, String path, Vector md5s, boolean forward, StringBuffer CrushAuth, StringBuffer status, long localSize) throws Exception {
        VRL u = new VRL(String.valueOf(url) + "WebInterface/function/?c2f=" + CrushAuth.toString().substring(CrushAuth.toString().length() - 4) + "&command=getMd5s&path=" + Base64.encodeBytes(path.getBytes("UTF8")) + "&forward=" + forward + "&local_size=" + localSize);
        com.crushftp.client.HttpURLConnection urlc = (com.crushftp.client.HttpURLConnection)u.openConnection();
        try {
            urlc.setReadTimeout(300000);
            urlc.setRequestMethod("GET");
            urlc.setRequestProperty("Cookie", "CrushAuth=" + CrushAuth.toString() + ";");
            urlc.setUseCaches(false);
            urlc.setDoInput(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
            Vector<String> md5Strings = new Vector<String>();
            String data = "";
            while ((data = br.readLine()) != null && status.length() == 0) {
                md5Strings.addElement(data);
                Tunnel2.parseMd5String(md5Strings, md5s);
            }
            br.close();
        }
        finally {
            urlc.disconnect();
        }
    }

    public static void getLocalMd5s(File f, boolean forward, StringBuffer status, Vector md5s) throws Exception {
        Thread.currentThread().setName("getLocalMD5s:" + f);
        Tunnel2.getInputStreamMd5s(new FileInputStream(f), f.length(), forward, status, md5s);
    }

    public static void getInputStreamMd5s(final InputStream in_f, final long file_size, final boolean forward, final StringBuffer status, Vector md5s) throws Exception {
        final Vector md5Strings = new Vector();
        final StringBuffer status2 = new StringBuffer();
        new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    try {
                        Tunnel2.getMd5s(in_f, true, forward, file_size, md5Strings, status, file_size);
                    }
                    catch (Exception e) {
                        Common.log("TUNNEL", 1, e);
                        try {
                            in_f.close();
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        status2.append("done");
                    }
                }
                finally {
                    try {
                        in_f.close();
                    }
                    catch (Exception exception) {}
                    status2.append("done");
                }
            }
        }).start();
        while (status2.length() == 0 || md5Strings.size() > 0) {
            if (md5Strings.size() > 0) {
                Tunnel2.parseMd5String(md5Strings, md5s);
            } else {
                Thread.sleep(100L);
            }
            if (status.length() > 0) break;
        }
    }

    public static void parseMd5String(Vector md5Strings, Vector md5s) throws Exception {
        while (md5Strings.size() > 0) {
            String data = md5Strings.remove(0).toString();
            if (data.equals("")) continue;
            Properties p = new Properties();
            p.put("md5", data.substring(data.indexOf(":") + 1).trim());
            p.put("start", data.substring(0, data.indexOf("-")).trim());
            p.put("end", data.substring(data.indexOf("-") + 1, data.indexOf(":")).trim());
            p.put("size", String.valueOf(Long.parseLong(p.getProperty("end")) - Long.parseLong(p.getProperty("start"))));
            md5s.addElement(p);
        }
    }

    public static Vector compareMd5s(Vector chunksF1, Vector chunksF2, boolean download) {
        boolean sameSize;
        Vector<Properties> changedChunks = new Vector<Properties>();
        boolean bl = sameSize = chunksF1.size() == chunksF2.size();
        if (sameSize && chunksF1.size() > 0) {
            sameSize = Long.parseLong(((Properties)chunksF1.elementAt(chunksF1.size() - 1)).getProperty("end")) == Long.parseLong(((Properties)chunksF2.elementAt(chunksF2.size() - 1)).getProperty("end"));
        }
        long firstBadByte = -1L;
        long lastBadByte = -1L;
        int x = 0;
        while (x < chunksF1.size() && x < chunksF2.size()) {
            boolean diff;
            Properties chunk1 = (Properties)chunksF1.elementAt(x);
            Properties chunk2 = (Properties)chunksF2.elementAt(x);
            boolean bl2 = diff = !chunk1.getProperty("md5").equals(chunk2.getProperty("md5")) || !chunk1.getProperty("size").equals(chunk2.getProperty("size"));
            if (diff && !sameSize && firstBadByte < 0L) {
                firstBadByte = Long.parseLong(chunk1.getProperty("start"));
            } else if (diff && chunk1.getProperty("size").equals(chunk2.getProperty("size"))) {
                changedChunks.addElement(chunk1);
            }
            if (diff) {
                Tunnel2.msg(String.valueOf(x) + ":" + chunk1 + (diff ? "***" : ""));
            }
            if (diff) {
                Tunnel2.msg(String.valueOf(x) + ":" + chunk2 + (diff ? "***" : ""));
            }
            if (firstBadByte >= 0L) break;
            ++x;
        }
        if (!sameSize) {
            changedChunks.removeAllElements();
            if (firstBadByte < 0L && chunksF1.size() > 0) {
                firstBadByte = Long.parseLong(((Properties)chunksF1.elementAt(chunksF1.size() - 1)).getProperty("start"));
            } else if (firstBadByte < 0L) {
                firstBadByte = 0L;
            }
            Properties chunk = new Properties();
            chunk.put("start", String.valueOf(firstBadByte));
            if (lastBadByte < 0L && chunksF1.size() > 0 && download) {
                lastBadByte = Long.parseLong(((Properties)chunksF1.elementAt(chunksF1.size() - 1)).getProperty("end"));
            } else if (lastBadByte < 0L && chunksF2.size() > 0 && !download) {
                lastBadByte = 0L;
            }
            chunk.put("end", String.valueOf(lastBadByte));
            changedChunks.addElement(chunk);
        }
        Vector<String> byteRanges = new Vector<String>();
        int x2 = 0;
        while (x2 < changedChunks.size()) {
            Properties chunk = (Properties)changedChunks.elementAt(x2);
            if (Long.parseLong(chunk.getProperty("end")) - 1L < 0L) {
                byteRanges.addElement(String.valueOf(chunk.getProperty("start")) + "-");
            } else {
                byteRanges.addElement(String.valueOf(chunk.getProperty("start")) + "-" + Long.parseLong(chunk.getProperty("end")));
            }
            Tunnel2.msg("Changed:" + x2 + ":" + chunk);
            ++x2;
        }
        return byteRanges;
    }

    public static String consumeResponse(HttpURLConnection urlc) throws Exception {
        InputStream in = urlc.getInputStream();
        byte[] b = DProperties.getArray();
        int bytesRead = 0;
        String result = "";
        while (bytesRead >= 0) {
            bytesRead = in.read(b);
            if (bytesRead <= 0) continue;
            result = String.valueOf(result) + new String(b, 0, bytesRead, "UTF8");
        }
        in.close();
        b = DProperties.releaseArray(b);
        if (result.indexOf("<response>") >= 0) {
            result = result.substring(result.indexOf("<response>") + "<response>".length(), result.indexOf("</response>"));
        }
        return result;
    }

    public static void doMD5Comparisons(String direction, Properties statusInfo, final String path3, final Properties controller, final Vector chunksF1, final Vector chunksF2, final StringBuffer CrushAuth, final StringBuffer status1, final StringBuffer status2, final File f, Vector byteRanges, StringBuffer action) throws Exception {
        Thread keepTunnelActiveThread = null;
        if (!controller.getProperty("URL_REAL", controller.getProperty("URL")).equals(controller.getProperty("URL"))) {
            keepTunnelActiveThread = Tunnel2.startPingThread(controller, CrushAuth, status1);
        }
        try {
            statusInfo.put(String.valueOf(direction) + "Status", String.valueOf(direction) + ": Getting MD5s for " + path3);
            new Thread(new Runnable(){

                @Override
                public void run() {
                    Thread.currentThread().setName("GetRemoteMD5s:" + path3);
                    try {
                        try {
                            Tunnel2.getRemoteMd5s(controller.getProperty("URL_REAL", controller.getProperty("URL")), path3, chunksF1, true, CrushAuth, status1, f.length());
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            status1.append("done");
                            if (chunksF1.size() == 0) {
                                status2.append("skip");
                            }
                        }
                    }
                    finally {
                        status1.append("done");
                        if (chunksF1.size() == 0) {
                            status2.append("skip");
                        }
                    }
                }
            }).start();
            new Thread(new Runnable(){

                @Override
                public void run() {
                    Thread.currentThread().setName("GetLocalMD5s:" + f);
                    try {
                        try {
                            Tunnel2.getLocalMd5s(f, true, status2, chunksF2);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            status2.append("done");
                            if (chunksF2.size() == 0) {
                                status1.append("skip");
                            }
                        }
                    }
                    finally {
                        status2.append("done");
                        if (chunksF2.size() == 0) {
                            status1.append("skip");
                        }
                    }
                }
            }).start();
            while ((status1.length() == 0 || status2.length() == 0) && Tunnel2.checkAction(action)) {
                if (status2.length() > 0 && chunksF1.size() > chunksF2.size()) {
                    status1.setLength(0);
                    status1.append("done");
                } else if (status1.length() > 0 && chunksF2.size() > chunksF1.size()) {
                    status2.setLength(0);
                    status2.append("done");
                }
                Thread.sleep(100L);
            }
            if (chunksF1.size() > 0) {
                byteRanges.removeAllElements();
                byteRanges.addAll(Tunnel2.compareMd5s(chunksF1, chunksF2, false));
            }
        }
        finally {
            status1.append("done");
            status2.append("done");
            if (keepTunnelActiveThread != null) {
                keepTunnelActiveThread.interrupt();
            }
        }
    }

    public static boolean checkAction(StringBuffer action) {
        int loops = 0;
        while (action.toString().equals("pause")) {
            try {
                Thread.sleep(1000L);
            }
            catch (Exception exception) {
                // empty catch block
            }
            if (loops++ > 10) break;
        }
        return !action.toString().equals("cancel");
    }

    public static Thread startPingThread(final Properties controller, final StringBuffer CrushAuth, final StringBuffer status) {
        Thread keepTunnelActiveThread = new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    while (status.length() == 0) {
                        Thread.sleep(5000L);
                        if (status.length() <= 0) {
                            VRL u = new VRL(String.valueOf(controller.getProperty("URL")) + "?c2f=" + CrushAuth.toString().substring(CrushAuth.toString().length() - 4) + "&command=ping");
                            com.crushftp.client.HttpURLConnection urlc = (com.crushftp.client.HttpURLConnection)u.openConnection();
                            urlc.setRequestMethod("GET");
                            urlc.setRequestProperty("Cookie", "CrushAuth=" + CrushAuth.toString() + ";");
                            urlc.setUseCaches(false);
                            Common.consumeResponse(urlc.getInputStream());
                            urlc.disconnect();
                            continue;
                        }
                        break;
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
        });
        keepTunnelActiveThread.start();
        return keepTunnelActiveThread;
    }

    public synchronized Chunk makeCommand(int qid, String command) throws UnsupportedEncodingException {
        return new Chunk(qid, command.getBytes("UTF8"), command.length(), this.commandNum--);
    }

    public static void writeAck(Chunk c, Queue q, Tunnel2 t) throws IOException {
        if (!c.isCommand() || c.getCommand().startsWith("CONNECT:") || c.getCommand().startsWith("END:") || c.getCommand().startsWith("CLOSEIN:")) {
            q.writeLocal(t.makeCommand(q.id, "A:" + c.num), -1);
        }
    }
}

