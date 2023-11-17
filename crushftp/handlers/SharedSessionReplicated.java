/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.File_S;
import com.crushftp.client.File_U;
import com.crushftp.client.GlacierClient;
import com.crushftp.client.S3CrushClient;
import com.crushftp.client.VRL;
import com.crushftp.client.Worker;
import crushftp.handlers.Common;
import crushftp.handlers.JobScheduler;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.SharedSession;
import crushftp.handlers.UserTools;
import crushftp.server.AdminControls;
import crushftp.server.ServerSessionAJAX;
import crushftp.server.ServerStatus;
import crushftp.server.daemon.DMZServerCommon;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import javax.net.ssl.SSLSocketFactory;

public class SharedSessionReplicated {
    public static Vector pending_pref_sync = new Vector();
    public static Vector pending_user_sync = new Vector();
    public static Vector pending_job_sync = new Vector();
    public static Vector pending_share_sync = new Vector();
    public static Properties send_queues = new Properties();
    public static Vector remote_host_ports = new Vector();
    static SSLSocketFactory factory = null;
    public static transient Object send_lock = new Object();
    static boolean offline = false;
    static Vector replicatedItems = new Vector();
    static long sync_delay = 0L;
    static long lastActive = 0L;
    static Properties pendingResponses = new Properties();
    static int reconnects = 0;
    static String our_hostname = "UNKNOWN";
    static ServerSocket ss = null;
    static String allowed_ips = "";
    static String replicate_session_host_port = "";
    static boolean enterprise_enabled = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void reset_sockets() {
        if (replicate_session_host_port.equals(ServerStatus.SG("replicate_session_host_port")) && enterprise_enabled == ServerStatus.siIG("enterprise_level") >= 0) {
            return;
        }
        if (!replicate_session_host_port.equals("")) {
            Vector vector = pending_pref_sync;
            synchronized (vector) {
                pending_pref_sync.clear();
                new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_pref_sync.xml").delete();
            }
            vector = pending_user_sync;
            synchronized (vector) {
                pending_user_sync.clear();
                new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_user_sync.xml").delete();
            }
            vector = pending_job_sync;
            synchronized (vector) {
                pending_job_sync.clear();
                new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_job_sync.xml").delete();
            }
            vector = pending_share_sync;
            synchronized (vector) {
                pending_share_sync.clear();
                new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_share_sync.xml").delete();
            }
        }
        enterprise_enabled = ServerStatus.siIG("enterprise_level") >= 0;
        replicate_session_host_port = ServerStatus.SG("replicate_session_host_port");
        remote_host_ports.clear();
        send_queues.clear();
        int x22 = 0;
        while (x22 < replicate_session_host_port.split(",").length) {
            if (!replicate_session_host_port.split(",")[x22].trim().equals("")) {
                if (replicate_session_host_port.indexOf(",") >= 0 && replicate_session_host_port.split(",")[x22].trim().toUpperCase().indexOf(our_hostname.toUpperCase()) < 0) {
                    remote_host_ports.addElement(replicate_session_host_port.split(",")[x22].trim());
                } else if (replicate_session_host_port.indexOf(",") < 0) {
                    remote_host_ports.addElement(replicate_session_host_port.split(",")[x22].trim());
                }
            }
            ++x22;
        }
        ServerStatus.thisObj.server_info.put("replicated_servers", "" + remote_host_ports);
        ServerStatus.thisObj.server_info.put("replicated_servers_count", String.valueOf(remote_host_ports.size()));
        try {
            if (ss != null) {
                ss.close();
            }
        }
        catch (IOException x22) {
            // empty catch block
        }
        ss = null;
        allowed_ips = ServerStatus.SG("replicated_server_ips");
        if (allowed_ips.equals("*")) {
            String new_allowed = ",";
            int x = 0;
            while (x < replicate_session_host_port.split(",").length) {
                String host = replicate_session_host_port.split(",")[x].split(":")[0].trim();
                try {
                    new_allowed = String.valueOf(new_allowed) + InetAddress.getByName(host).getHostAddress() + ",";
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.log("SERVER", 0, e);
                }
                ++x;
            }
            allowed_ips = new_allowed;
        }
        if (!enterprise_enabled) {
            Log.log("SERVER", 0, "Replication only allowed for enterprise licenses.");
        }
        if (remote_host_ports.size() == 0 || !enterprise_enabled) {
            return;
        }
        int xx = 0;
        while (xx < remote_host_ports.size()) {
            send_queues.put(remote_host_ports.elementAt(xx), new Vector());
            ++xx;
        }
        try {
            String remote_host_port = remote_host_ports.elementAt(0).toString();
            int bind_port = 0;
            bind_port = remote_host_port.split(":").length > 2 ? Integer.parseInt(remote_host_port.split(":")[0]) : Integer.parseInt(remote_host_port.split(":")[1]);
            if (System.getProperty("crushftp.sharedsession.ssl", "true").equals("true")) {
                ss = ServerStatus.thisObj.common_code.getServerSocket(bind_port, System.getProperty("crushftp.sharedsession.bindip", "0.0.0.0"), "builtin", "crushftp", "crushftp", "", false, 1, true, false, null);
                Common.configureSSLTLSSocket(ss);
            } else {
                ss = new ServerSocket(bind_port, 1000, InetAddress.getByName(System.getProperty("crushftp.sharedsession.bindip", "0.0.0.0")));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.log("SERVER", 0, e);
        }
    }

    public static void init() {
        Properties p;
        int x;
        try {
            replicatedItems.addElement("crushftp.usernames");
            replicatedItems.addElement("crushftp.sessions");
            try {
                our_hostname = InetAddress.getLocalHost().getHostName();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            SharedSessionReplicated.reset_sockets();
            Worker.startWorker(new Runnable(){

                @Override
                public void run() {
                    try {
                        String current_name = Thread.currentThread().getName();
                        while (true) {
                            try {
                                while (true) {
                                    if (ss != null) {
                                        if (current_name.equals("SharedSessionReplciatedReceiver")) {
                                            current_name = "SharedSessionReplicatedReceiver:" + remote_host_ports.elementAt(0) + ":";
                                        }
                                        final Socket sock = ss.accept();
                                        String incoming_ip = sock.getInetAddress().getHostAddress();
                                        if (!allowed_ips.equals("") && !com.crushftp.client.Common.do_search(allowed_ips, incoming_ip, false, 0) && allowed_ips.indexOf(incoming_ip) < 0) {
                                            String msg = "IP " + sock.getInetAddress().getHostAddress() + " was from an untrusted host and was denied replication control. Allowed IPs: " + allowed_ips;
                                            sock.close();
                                            System.out.println(msg);
                                            Log.log("SERVER", 0, msg);
                                            continue;
                                        }
                                        sock.setSoTimeout(10000);
                                        final String parent_thread_name = current_name;
                                        Worker.startWorker(new Runnable(){

                                            @Override
                                            public void run() {
                                                try {
                                                    Properties p;
                                                    ObjectInputStream ois = new ObjectInputStream(sock.getInputStream());
                                                    long received = 0L;
                                                    while (!(p = (Properties)ois.readObject()).getProperty("action").equalsIgnoreCase("CLOSE")) {
                                                        p.put("source_socket", "" + sock);
                                                        SharedSessionReplicated.receive(p);
                                                        Thread.currentThread().setName(String.valueOf(parent_thread_name) + " reconnects=" + reconnects + " received=" + ++received + " pending_pref_sync=" + pending_pref_sync.size() + " pending_user_sync=" + pending_user_sync.size() + " pending_job_sync=" + pending_job_sync.size() + " pendingResponses=" + pendingResponses);
                                                    }
                                                    ois.close();
                                                }
                                                catch (Exception e) {
                                                    Log.log("SERVER", 0, e);
                                                }
                                                try {
                                                    sock.close();
                                                }
                                                catch (IOException iOException) {
                                                    // empty catch block
                                                }
                                                ++reconnects;
                                            }
                                        });
                                        continue;
                                    }
                                    Thread.sleep(1000L);
                                }
                            }
                            catch (Exception e) {
                                Log.log("SERVER", 0, e);
                                Thread.sleep(1000L);
                                continue;
                            }
                            break;
                        }
                    }
                    catch (Exception e) {
                        Log.log("SERVER", 0, e);
                        return;
                    }
                }
            }, "SharedSessionReplicatedReceiver");
            Worker.startWorker(new Runnable(){

                /*
                 * WARNING - Removed try catching itself - possible behaviour change.
                 */
                @Override
                public void run() {
                    long lastSync = System.currentTimeMillis();
                    while (true) {
                        SharedSessionReplicated.flushNow();
                        boolean empty = true;
                        int x3 = 0;
                        while (x3 < 100) {
                            try {
                                Thread.sleep(10L);
                            }
                            catch (Exception exception) {
                                // empty catch block
                            }
                            Vector remote_host_ports2 = (Vector)remote_host_ports.clone();
                            int xx = 0;
                            while (xx < remote_host_ports2.size()) {
                                Vector v = (Vector)send_queues.get(remote_host_ports2.elementAt(xx));
                                if (v.size() > 0) {
                                    empty = false;
                                }
                                ++xx;
                            }
                            if (!empty) break;
                            ++x3;
                        }
                        try {
                            Properties p;
                            int x2;
                            if (System.currentTimeMillis() - lastSync <= 30000L || !empty) continue;
                            Vector x3 = pending_pref_sync;
                            synchronized (x3) {
                                Vector pending_pref_sync2 = (Vector)pending_pref_sync.clone();
                                x2 = 0;
                                while (x2 < pending_pref_sync2.size()) {
                                    p = (Properties)pending_pref_sync2.elementAt(x2);
                                    if (System.currentTimeMillis() - Long.parseLong(p.getProperty("time")) > 40000L) {
                                        pending_pref_sync.remove(p);
                                        SharedSessionReplicated.send(p.getProperty("id"), p.getProperty("action"), p.get("key"), p.get("val"));
                                    }
                                    ++x2;
                                }
                                if (pending_pref_sync2.size() != pending_pref_sync.size()) {
                                    SharedSessionReplicated.flushPendingPrefSync();
                                }
                                ServerStatus.thisObj.server_info.put("replicated_servers_pending_pref_sync", String.valueOf(pending_pref_sync.size()));
                            }
                            x3 = pending_user_sync;
                            synchronized (x3) {
                                Vector pending_user_sync2 = (Vector)pending_user_sync.clone();
                                x2 = 0;
                                while (x2 < pending_user_sync2.size()) {
                                    p = (Properties)pending_user_sync2.elementAt(x2);
                                    if (System.currentTimeMillis() - Long.parseLong(p.getProperty("time")) > 40000L) {
                                        pending_user_sync.remove(p);
                                        SharedSessionReplicated.send(p.getProperty("id"), p.getProperty("action"), p.get("key"), p.get("val"));
                                    }
                                    ++x2;
                                }
                                if (pending_user_sync2.size() != pending_user_sync.size()) {
                                    SharedSessionReplicated.flushPendingUserSync();
                                }
                                ServerStatus.thisObj.server_info.put("replicated_servers_pending_user_sync", String.valueOf(pending_user_sync.size()));
                            }
                            x3 = pending_job_sync;
                            synchronized (x3) {
                                Vector pending_job_sync2 = (Vector)pending_job_sync.clone();
                                x2 = 0;
                                while (x2 < pending_job_sync2.size()) {
                                    p = (Properties)pending_job_sync2.elementAt(x2);
                                    if (System.currentTimeMillis() - Long.parseLong(p.getProperty("time")) > 40000L) {
                                        pending_job_sync.remove(p);
                                        SharedSessionReplicated.send(p.getProperty("id"), p.getProperty("action"), p.get("key"), p.get("val"));
                                    }
                                    ++x2;
                                }
                                if (pending_job_sync2.size() != pending_job_sync.size()) {
                                    SharedSessionReplicated.flushPendingJobSync();
                                }
                                ServerStatus.thisObj.server_info.put("replicated_servers_pending_job_sync", String.valueOf(pending_job_sync.size()));
                            }
                            x3 = pending_share_sync;
                            synchronized (x3) {
                                Vector pending_share_sync2 = (Vector)pending_share_sync.clone();
                                x2 = 0;
                                while (x2 < pending_share_sync2.size()) {
                                    p = (Properties)pending_share_sync2.elementAt(x2);
                                    if (System.currentTimeMillis() - Long.parseLong(p.getProperty("time")) > 40000L) {
                                        pending_share_sync.remove(p);
                                        SharedSessionReplicated.send(p.getProperty("id"), p.getProperty("action"), p.get("key"), p.get("val"));
                                    }
                                    ++x2;
                                }
                                if (pending_share_sync2.size() != pending_share_sync.size()) {
                                    SharedSessionReplicated.flushPendingShareSync();
                                }
                                ServerStatus.thisObj.server_info.put("replicated_servers_pending_share_sync", String.valueOf(pending_share_sync.size()));
                            }
                            Enumeration<Object> keys = pendingResponses.keys();
                            while (keys.hasMoreElements()) {
                                String key = keys.nextElement().toString();
                                Properties val = (Properties)pendingResponses.get(key);
                                if (System.currentTimeMillis() - Long.parseLong(val.getProperty("time", "0")) <= 10000L) continue;
                                val.put("response_num", "-1");
                                pendingResponses.remove(key);
                            }
                            ServerStatus.thisObj.server_info.put("replicated_servers_pendingResponses", String.valueOf(pendingResponses.size()));
                            lastSync = System.currentTimeMillis();
                        }
                        catch (Exception exception) {
                            continue;
                        }
                        break;
                    }
                }
            }, "SharedSessionReplicatedSender:");
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
        if (new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_pref_sync.xml").exists()) {
            Vector pending_pref_sync2 = (Vector)Common.readXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_pref_sync.xml");
            if (pending_pref_sync2 != null) {
                x = 0;
                while (x < pending_pref_sync2.size()) {
                    p = (Properties)pending_pref_sync2.elementAt(x);
                    SharedSessionReplicated.send(p.getProperty("id"), p.getProperty("action"), p.get("key"), p.get("val"));
                    ++x;
                }
            }
            ServerStatus.thisObj.server_info.put("replicated_servers_pending_pref_sync", String.valueOf(pending_pref_sync.size()));
        }
        if (new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_user_sync.xml").exists()) {
            Vector pending_user_sync2 = (Vector)Common.readXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_user_sync.xml");
            if (pending_user_sync2 != null) {
                x = 0;
                while (x < pending_user_sync2.size()) {
                    p = (Properties)pending_user_sync2.elementAt(x);
                    SharedSessionReplicated.send(p.getProperty("id"), p.getProperty("action"), p.get("key"), p.get("val"));
                    ++x;
                }
            }
            ServerStatus.thisObj.server_info.put("replicated_servers_pending_user_sync", String.valueOf(pending_user_sync.size()));
        }
        if (new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_job_sync.xml").exists()) {
            Vector pending_job_sync2 = (Vector)Common.readXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_job_sync.xml");
            if (pending_job_sync2 != null) {
                x = 0;
                while (x < pending_job_sync2.size()) {
                    p = (Properties)pending_job_sync2.elementAt(x);
                    SharedSessionReplicated.send(p.getProperty("id"), p.getProperty("action"), p.get("key"), p.get("val"));
                    ++x;
                }
            }
            ServerStatus.thisObj.server_info.put("replicated_servers_pending_job_sync", String.valueOf(pending_job_sync.size()));
        }
        if (new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_share_sync.xml").exists()) {
            Vector pending_share_sync2 = (Vector)Common.readXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_share_sync.xml");
            if (pending_share_sync2 != null) {
                x = 0;
                while (x < pending_share_sync2.size()) {
                    p = (Properties)pending_share_sync2.elementAt(x);
                    SharedSessionReplicated.send(p.getProperty("id"), p.getProperty("action"), p.get("key"), p.get("val"));
                    ++x;
                }
            }
            ServerStatus.thisObj.server_info.put("replicated_servers_pending_share_sync", String.valueOf(pending_share_sync.size()));
        }
    }

    public static void flushWait() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void flushNow() {
        boolean dofull = false;
        Object object = send_lock;
        synchronized (object) {
            Vector remote_host_ports2 = (Vector)remote_host_ports.clone();
            int xx = 0;
            while (xx < remote_host_ports2.size()) {
                lastActive = System.currentTimeMillis();
                Vector send_queue = (Vector)send_queues.get(remote_host_ports2.elementAt(xx));
                try {
                    if (factory == null && System.getProperty("crushftp.sharedsession.ssl", "true").equals("true")) {
                        factory = ServerStatus.thisObj.common_code.getSSLContext("builtin", null, "crushftp", "crushftp", "TLS", false, true).getSocketFactory();
                    }
                    if (send_queue.size() > 0) {
                        Properties p;
                        Socket sock = null;
                        String remote_host_port = remote_host_ports2.elementAt(xx).toString();
                        sock = System.getProperty("crushftp.sharedsession.ssl", "true").equals("true") ? (remote_host_port.split(":").length > 2 ? factory.createSocket(remote_host_port.split(":")[1], Integer.parseInt(remote_host_port.split(":")[2])) : factory.createSocket(remote_host_port.split(":")[0], Integer.parseInt(remote_host_port.split(":")[1]))) : new Socket(remote_host_port.split(":")[0], Integer.parseInt(remote_host_port.split(":")[1]));
                        sock.setSoTimeout(10000);
                        ObjectOutputStream oos = new ObjectOutputStream(sock.getOutputStream());
                        while (send_queue.size() > 0) {
                            Thread.currentThread().setName(String.valueOf(Thread.currentThread().getName().substring(0, Thread.currentThread().getName().lastIndexOf(":") + 1)) + send_queue.size());
                            p = (Properties)send_queue.elementAt(0);
                            if (!p.getProperty("action", "").equals("crushftp.session.update")) {
                                Log.log("SERVER", 2, "SharedSession:Send:" + p.getProperty("id") + ":" + p.getProperty("action") + ":" + p.getProperty("key") + ":" + p.getProperty("size", "0") + " bytes");
                            }
                            oos.writeObject(p);
                            oos.flush();
                            send_queue.remove(0);
                            sync_delay = System.currentTimeMillis() - Long.parseLong(p.getProperty("queued"));
                            lastActive = System.currentTimeMillis();
                        }
                        p = new Properties();
                        p.put("action", "CLOSE");
                        oos.writeObject(p);
                        oos.flush();
                        oos.close();
                        sock.close();
                        offline = false;
                        lastActive = System.currentTimeMillis();
                        ServerStatus.thisObj.server_info.put("replicated_servers_lastActive", String.valueOf(lastActive));
                        ServerStatus.thisObj.server_info.put("replicated_servers_sent_" + xx, String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_servers_" + xx, "0")) + 1));
                    }
                }
                catch (SocketException e) {
                    lastActive = 0L;
                    send_queue.removeAllElements();
                    dofull = true;
                }
                catch (Exception e) {
                    lastActive = 0L;
                    Log.log("SERVER", 0, e);
                    dofull = true;
                }
                ServerStatus.thisObj.server_info.put("replicated_servers_queue_" + xx, String.valueOf(send_queue.size()));
                ++xx;
            }
        }
        if (dofull) {
            SharedSessionReplicated.doFullSync();
        }
    }

    public static void doFullSync() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void receive(Properties p) {
        if (!enterprise_enabled) {
            return;
        }
        long ms = System.currentTimeMillis() - Long.parseLong(p.getProperty("queued"));
        if (!p.getProperty("action", "").equals("crushftp.session.update")) {
            Log.log("SERVER", 2, "SharedSession:Receive:" + p.getProperty("id") + ":" + p.getProperty("action") + ":" + p.getProperty("key") + ":" + p.getProperty("size", "0") + " bytes:" + ms + "ms");
        }
        ServerStatus.thisObj.server_info.put("replicated_received_message_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_received_message_count", "0")) + 1));
        if (p.getProperty("action").equalsIgnoreCase("RESPONSE")) {
            if (pendingResponses.containsKey(p.getProperty("id"))) {
                Properties val = (Properties)pendingResponses.remove(p.getProperty("id"));
                int response_num = Integer.parseInt(val.getProperty("response_num", "0"));
                val.putAll((Map<?, ?>)p);
                val.put("response_num", String.valueOf(++response_num));
                ServerStatus.thisObj.server_info.put("replicated_servers_pendingResponses", String.valueOf(pendingResponses.size()));
            }
            int x = pending_pref_sync.size() - 1;
            while (x >= 0) {
                Properties pp = (Properties)pending_pref_sync.elementAt(x);
                if (p.getProperty("id").equals(pp.getProperty("id"))) {
                    pending_pref_sync.remove(x);
                    SharedSessionReplicated.flushPendingPrefSync();
                }
                --x;
            }
            ServerStatus.thisObj.server_info.put("replicated_servers_pending_pref_sync", String.valueOf(pending_pref_sync.size()));
            x = pending_user_sync.size() - 1;
            while (x >= 0) {
                Properties pp = (Properties)pending_user_sync.elementAt(x);
                if (p.getProperty("id").equals(pp.getProperty("id"))) {
                    pending_user_sync.remove(x);
                    SharedSessionReplicated.flushPendingUserSync();
                }
                --x;
            }
            ServerStatus.thisObj.server_info.put("replicated_servers_pending_user_sync", String.valueOf(pending_user_sync.size()));
            x = pending_job_sync.size() - 1;
            while (x >= 0) {
                Properties pp = (Properties)pending_job_sync.elementAt(x);
                if (p.getProperty("id").equals(pp.getProperty("id"))) {
                    pending_job_sync.remove(x);
                    SharedSessionReplicated.flushPendingJobSync();
                }
                --x;
            }
            ServerStatus.thisObj.server_info.put("replicated_servers_pending_job_sync", String.valueOf(pending_job_sync.size()));
            x = pending_share_sync.size() - 1;
            while (x >= 0) {
                Properties pp = (Properties)pending_share_sync.elementAt(x);
                if (p.getProperty("id").equals(pp.getProperty("id"))) {
                    pending_share_sync.remove(x);
                    SharedSessionReplicated.flushPendingShareSync();
                }
                --x;
            }
            ServerStatus.thisObj.server_info.put("replicated_servers_pending_share_sync", String.valueOf(pending_share_sync.size()));
        } else if (p.getProperty("action", "").equals("WRITE_PREFS")) {
            Properties prefs = (Properties)p.remove("val");
            try {
                Common.write_server_settings(prefs, p.getProperty("key"));
                DMZServerCommon.last_prefs_time.put(p.getProperty("key"), String.valueOf(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "prefs_" + p.getProperty("key") + ".XML").lastModified()));
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_write_prefs_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_write_prefs_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.AdminControls.setServerItem")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.setServerItem((Properties)pp.remove("request"), (String)pp.remove("site"), false);
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_write_prefs_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_write_prefs_count", "0")) + 1));
        } else if (p.getProperty("action", "").startsWith("BAN_IP")) {
            try {
                Properties params = (Properties)((Properties)p.get("val")).get("params");
                if (p.getProperty("action", "").endsWith("V4")) {
                    ServerStatus.thisObj.ban_ipv4(params.getProperty("ip"), Integer.parseInt(params.getProperty("timeout")), params.getProperty("onlyRealBan").equals("true"), params.getProperty("reason"), false);
                } else if (p.getProperty("action", "").endsWith("V6")) {
                    ServerStatus.thisObj.ban_ipv6(params.getProperty("ip"), Integer.parseInt(params.getProperty("timeout")), params.getProperty("onlyRealBan").equals("true"), params.getProperty("reason"), false);
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_write_prefs_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_write_prefs_count", "0")) + 1));
        } else if (p.getProperty("action", "").startsWith("SYNC_CRUSHSSO_TOKENS")) {
            try {
                Properties tokens = (Properties)p.get("val");
                Properties crushSSO_tokens = ServerStatus.siPG("crushSSO_tokens");
                if (crushSSO_tokens == null) {
                    crushSSO_tokens = new Properties();
                    ServerStatus.siPUT("crushSSO_tokens", crushSSO_tokens);
                }
                Enumeration<Object> keys = tokens.keys();
                while (keys.hasMoreElements()) {
                    String key = "" + keys.nextElement();
                    if (crushSSO_tokens.containsKey(key)) continue;
                    crushSSO_tokens.put(key, tokens.get(key));
                    Log.log("SERVER", 0, "CrushSSO:Added replicated token:" + key);
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").startsWith("SYNC_LOGIN_FREQUENCY")) {
            try {
                Properties login_frequency;
                Properties sync_info = (Properties)p.get("val");
                Properties keys = login_frequency = ServerStatus.siPG("login_frequency");
                synchronized (keys) {
                    login_frequency.put(sync_info.getProperty("user_name").toLowerCase(), (Properties)sync_info.get("login_prop"));
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.AdminControls.saveReport")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.saveReport((Properties)pp.remove("request"), "", false);
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.AdminControls.renameJob")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.renameJob((Properties)pp.remove("request"), (String)pp.remove("site"), false);
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_job_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_job_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.AdminControls.removeJob")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.removeJob((Properties)pp.remove("request"), (String)pp.remove("site"), false);
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_job_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_job_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.AdminControls.addJob")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.addJob((Properties)pp.remove("request"), (String)pp.remove("site"), false);
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_job_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_job_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.AdminControls.newFolder")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.newFolder((Properties)pp.remove("request"), (String)pp.remove("site"), false);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.AdminControls.renameItem")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.renameItem((Properties)pp.remove("request"), (String)pp.remove("site"), false);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.AdminControls.duplicateItem")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.duplicateItem((Properties)pp.remove("request"), (String)pp.remove("site"), false);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.AdminControls.deleteItem")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.deleteItem((Properties)pp.remove("request"), (String)pp.remove("site"), false);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.s3CrushClient.writeFs")) {
            try {
                Properties pp = (Properties)p.get("val");
                S3CrushClient.writeFs(System.getProperty("crushftp.s3_root", "./s3/"), pp.getProperty("bucketName0"), null, pp.getProperty("path"), (Properties)pp.get("data"));
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.glacierCrushClient.writeFs")) {
            try {
                Properties pp = (Properties)p.get("val");
                GlacierClient.writeFs(System.getProperty("crushftp.glacier_root", "./glacier/"), pp.getProperty("vaultName0"), null, pp.getProperty("path"), (Properties)pp.get("data"));
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.JobScheduler.jobRunning")) {
            try {
                String id = p.getProperty("id");
                Properties val = (Properties)p.get("val");
                boolean ok = JobScheduler.jobRunning(val.getProperty("scheduleName"));
                Properties response = new Properties();
                response.put("scheduleName", val.getProperty("scheduleName"));
                response.put("running_" + Common.makeBoundary(), String.valueOf(ok));
                SharedSessionReplicated.send(id, "RESPONSE", "", response);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.JobScheduler.runJob")) {
            try {
                final Properties p_f2 = p;
                final Properties info = (Properties)p.remove("val");
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        Vector items;
                        byte[] b = null;
                        Properties event = (Properties)info.remove("data");
                        Log.log("SERVER", 0, "crushftp.JobScheduler.runJob:" + info.getProperty("use_dmz", "") + ":" + p_f2.getProperty("source_socket", "") + ":" + event.getProperty("scheduleName"));
                        if (info.getProperty("use_dmz", "").equalsIgnoreCase("All DMZ Recursive")) {
                            items = (Vector)info.remove("items");
                            Enumeration<Object> keys = DMZServerCommon.dmzInstances.keys();
                            while (keys.hasMoreElements()) {
                                Vector log = new Vector();
                                try {
                                    String the_dmz = keys.nextElement().toString();
                                    Log.log("SERVER", 0, "crushftp.JobScheduler.runJob:" + info.getProperty("use_dmz", "") + ":" + p_f2.getProperty("source_socket", "") + ":" + event.getProperty("scheduleName") + ":" + the_dmz);
                                    items = SharedSessionReplicated.runOnDmz(event, the_dmz, items, log);
                                    info.put("items", items);
                                    Vector log2 = (Vector)log.clone();
                                    String log_str = "";
                                    while (log2.size() > 0) {
                                        log_str = String.valueOf(log_str) + log2.remove(0) + "\r\n";
                                    }
                                    Log.log("SERVER", 1, log_str);
                                    b = log_str.getBytes("UTF8");
                                }
                                catch (Exception e) {
                                    Log.log("SERVER", 0, e);
                                }
                            }
                        } else {
                            items = (Vector)info.remove("items");
                            event.put("event_plugin_list", "CrushTask (User Defined)");
                            event.put("name", event.getProperty("scheduleName"));
                            Properties info2 = ServerStatus.thisObj.events6.doEventPlugin(null, event, null, items);
                            p_f2.put("data", info2);
                            try {
                                RandomAccessFile raf = new RandomAccessFile(new File_S(info2.getProperty("log_file")), "r");
                                b = new byte[(int)raf.length()];
                                raf.readFully(b);
                                raf.close();
                            }
                            catch (Throwable e) {
                                Log.log("SERVER", 0, e);
                                b = new byte[]{};
                            }
                        }
                        String uid = Common.makeBoundary();
                        p_f2.put("log", new String(b));
                        p_f2.put("type", "RESPONSE");
                        p_f2.put("log_" + uid, p_f2.remove("log"));
                        p_f2.put("data_" + uid, p_f2.remove("data"));
                        SharedSessionReplicated.send(p_f2.getProperty("id"), "RESPONSE", "", p_f2);
                    }
                });
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_job_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_job_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").startsWith("crushftp.handlers.")) {
            String id;
            block158: {
                id = p.getProperty("id");
                try {
                    String action = p.getProperty("action");
                    p = (Properties)p.remove("val");
                    Log.log("SERVER", 2, "" + p);
                    if (action.endsWith(".writeGroups")) {
                        Log.log("SERVER", 0, String.valueOf(action) + ":" + p.getProperty("serverGroup"));
                        UserTools.writeGroups(p.getProperty("serverGroup"), (Properties)p.get("groups"), false);
                        break block158;
                    }
                    if (action.endsWith(".writeUser")) {
                        Log.log("SERVER", 0, String.valueOf(action) + ":" + p.getProperty("serverGroup") + ":" + p.getProperty("username") + ":" + p.getProperty("backup", ""));
                        UserTools.writeUser(p.getProperty("serverGroup"), p.getProperty("username"), (Properties)p.get("user"), false, p.getProperty("backup", "").equals("true"));
                        break block158;
                    }
                    if (action.endsWith(".writeInheritance")) {
                        Log.log("SERVER", 0, String.valueOf(action) + ":" + p.getProperty("serverGroup"));
                        UserTools.writeInheritance(p.getProperty("serverGroup"), (Properties)p.get("inheritance"), false);
                        break block158;
                    }
                    if (action.endsWith(".deleteUser")) {
                        Log.log("SERVER", 0, String.valueOf(action) + ":" + p.getProperty("serverGroup") + ":" + p.getProperty("username"));
                        UserTools.deleteUser(p.getProperty("serverGroup"), p.getProperty("username"), false);
                        break block158;
                    }
                    if (action.endsWith(".addFolder")) {
                        Log.log("SERVER", 0, String.valueOf(action) + ":" + p.getProperty("serverGroup") + ":" + p.getProperty("username") + ":" + p.getProperty("path") + ":" + p.getProperty("name"));
                        UserTools.addFolder(p.getProperty("serverGroup"), p.getProperty("username"), p.getProperty("path"), p.getProperty("name"), false);
                        break block158;
                    }
                    if (action.endsWith(".addItem")) {
                        try {
                            Log.log("SERVER", 0, String.valueOf(action) + ":" + p.getProperty("serverGroup") + ":" + p.getProperty("username") + ":" + p.getProperty("path") + ":" + p.getProperty("name") + ":" + new VRL(p.getProperty("url")).safe());
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 1, e);
                        }
                        UserTools.addItem(p.getProperty("serverGroup"), p.getProperty("username"), p.getProperty("path"), p.getProperty("name"), p.getProperty("url"), p.getProperty("type"), (Properties)p.get("moreItems"), p.getProperty("encrypted").equals("true"), p.getProperty("encrypted_class"), false);
                        break block158;
                    }
                    if (action.endsWith(".writeVFS")) {
                        Log.log("SERVER", 0, String.valueOf(action) + ":" + p.getProperty("serverGroup") + ":" + p.getProperty("username"));
                        UserTools.writeVFS(p.getProperty("serverGroup"), p.getProperty("username"), (Properties)p.get("virtual"), false);
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, e);
                }
            }
            SharedSessionReplicated.send(id, "RESPONSE", "", null);
            ServerStatus.thisObj.server_info.put("replicated_user_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_user_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.session.update")) {
            try {
                SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(p.getProperty("id"));
                if (thisSession != null) {
                    thisSession.put(p.getProperty("key"), p.get("val"), false);
                }
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.server.ServerSessionAjax.doFileAbortBlock")) {
            try {
                final Properties p_f2 = p;
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        Properties pp = (Properties)p_f2.get("val");
                        SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").get(pp.remove("CrushAuth"));
                        try {
                            if (thisSession != null) {
                                thisSession.doFileAbortBlock((String)pp.remove("the_command_data"), false);
                            }
                        }
                        catch (Exception e) {
                            Log.log("SERVER", 0, e);
                        }
                    }
                });
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
        } else if (p.getProperty("action", "").equals("crushftp.session.remove_user")) {
            block159: {
                try {
                    SessionCrush thisSession = (SessionCrush)SharedSession.find("crushftp.sessions").remove(p.getProperty("id"), false);
                    if (thisSession != null) {
                        ServerStatus.thisObj.remove_user(thisSession.user_info, true);
                        break block159;
                    }
                    ServerStatus e = ServerStatus.thisObj;
                    synchronized (e) {
                        int loops = 0;
                        while (loops < 30) {
                            try {
                                int x = ServerStatus.siVG("user_list").size() - 1;
                                while (x >= 0) {
                                    Properties user_info = (Properties)ServerStatus.siVG("user_list").elementAt(x);
                                    if (user_info.getProperty("CrushAuth", "").equals(p.getProperty("id"))) {
                                        ServerStatus.thisObj.remove_user(user_info);
                                    }
                                    --x;
                                }
                                break;
                            }
                            catch (Exception exception) {
                                Thread.sleep(100L);
                                ++loops;
                            }
                        }
                    }
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, e);
                }
            }
            ServerStatus.thisObj.server_info.put("replicated_user_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_user_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.share.create")) {
            try {
                String action = p.getProperty("action");
                Properties pp = (Properties)p.remove("val");
                Log.log("SERVER", 2, "" + p);
                new File_U(String.valueOf(pp.getProperty("userHome")) + "VFS/").mkdirs();
                new File_U(pp.getProperty("userStorage")).mkdirs();
                Common.writeXMLObject_U(String.valueOf(pp.getProperty("userHome")) + "VFS.XML", pp.get("permissions"), "VFS");
                Common.writeXMLObject_U(String.valueOf(pp.getProperty("userHome")) + "INFO.XML", pp.get("request"), "INFO");
                Common.writeXMLObject_U(String.valueOf(pp.getProperty("userHome")) + "VFS/" + pp.getProperty("itemName") + pp.getProperty("uid"), pp.get("v"), "VFS");
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_user_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_user_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.share.removeTempAccount")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.removeTempAccount((Properties)pp.remove("request"), (String)pp.remove("site"), false);
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_share_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_share_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.share.removeTempAccountFile")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.removeTempAccountFile((Properties)pp.remove("request"), (String)pp.remove("site"), false);
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_share_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_share_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.share.addTempAccount")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.addTempAccount((Properties)pp.remove("request"), (String)pp.remove("site"), false);
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_share_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_share_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.share.addTempAccountFile")) {
            try {
                Properties pp = (Properties)p.get("val");
                AdminControls.addTempAccountFile((Properties)pp.remove("request"), (String)pp.remove("site"), false);
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_share_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_share_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.delete.share")) {
            try {
                Properties pp = (Properties)p.get("val");
                ServerSessionAJAX.deleteShare((Properties)pp.remove("request"), (String)pp.remove("userGroup"), (String)pp.remove("username"));
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_share_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_share_changes_count", "0")) + 1));
        } else if (p.getProperty("action", "").equals("crushftp.edit.share")) {
            try {
                Properties pp = (Properties)p.get("val");
                ServerSessionAJAX.editShare((Properties)pp.remove("request"), (String)pp.remove("username"), (Vector)pp.remove("web_customizations"));
                SharedSessionReplicated.send(p.getProperty("id"), "RESPONSE", "", null);
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            ServerStatus.thisObj.server_info.put("replicated_share_changes_count", String.valueOf(Integer.parseInt(ServerStatus.thisObj.server_info.getProperty("replicated_share_changes_count", "0")) + 1));
        } else {
            SharedSession ss = SharedSession.find(p.getProperty("id"));
            if (p.getProperty("action").equals("put")) {
                ss.put(p.get("key"), p.get("val"), false);
            } else if (p.getProperty("action").equals("remove")) {
                ss.remove(p.get("key"), false);
            } else if (p.getProperty("action").equals("anyPassToken")) {
                UserTools.addAnyPassToken(p.getProperty("val"));
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void send(String id1, String action, Object key, Object val) {
        if (!enterprise_enabled || remote_host_ports.size() == 0) {
            return;
        }
        try {
            Properties p = new Properties();
            p.put("action", action);
            p.put("id", id1);
            p.put("key", key);
            if (val != null) {
                p.put("val", val);
            }
            Log.log("SERVER", 2, "SHAREDSESSION:" + id1 + ":" + action + ":" + key);
            p.put("time", String.valueOf(System.currentTimeMillis()));
            byte[] b = null;
            Object object = SharedSession.sessionLock;
            synchronized (object) {
                b = com.crushftp.client.Common.CLONE1(p);
            }
            Properties p2 = (Properties)com.crushftp.client.Common.CLONE2(b);
            p2.put("size", String.valueOf(b.length));
            Vector remote_host_ports2 = (Vector)remote_host_ports.clone();
            String allowed_hosts = "";
            try {
                Properties request;
                if (val != null && val instanceof Properties && (request = (Properties)((Properties)val).get("request")) != null) {
                    String[] ui_save_preferences = request.getProperty("ui_save_preferences", "").split(";");
                    String ui_save_preferences_item = request.getProperty("ui_save_preferences_item", "");
                    if (!request.containsKey("ui_save_preferences")) {
                        ui_save_preferences = new String[]{request.getProperty("ui_save_preferences_item", "")};
                        ui_save_preferences_item = ui_save_preferences_item.split("=")[0];
                    }
                    int x = 0;
                    while (x < ui_save_preferences.length) {
                        if (!ui_save_preferences[x].equals("")) {
                            String ui_item = ui_save_preferences[x].split("=")[0].trim();
                            if (ui_item.endsWith(":")) {
                                ui_item = ui_item.substring(0, ui_item.length() - 1);
                            }
                            if (ui_item.equals(ui_save_preferences_item)) {
                                allowed_hosts = "," + ui_save_preferences[x].split("=")[1].trim() + ",";
                            }
                        }
                        ++x;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.log("SERVER", 0, e);
            }
            int xx = 0;
            while (xx < remote_host_ports2.size()) {
                String id2 = id1;
                String remote_host_port = remote_host_ports2.elementAt(xx).toString();
                String remote_host = remote_host_port.split(":")[0].trim();
                String remote_host_only = "";
                if (id2.indexOf(":") >= 0 && !action.equals("RESPONSE")) {
                    remote_host_only = id2.split(":")[1].trim();
                }
                if (remote_host_only.equals("") || remote_host.equals(remote_host_only)) {
                    if (id2.indexOf(":") < 0) {
                        id2 = String.valueOf(id2) + ":" + remote_host;
                    }
                    p2.put("id", id2);
                    if (allowed_hosts.equals("") || allowed_hosts.indexOf("," + remote_host + ",") >= 0) {
                        Vector vector;
                        if (action.startsWith("crushftp.AdminControls.setServerItem")) {
                            if (ServerStatus.BG("replicate_preferences")) {
                                p2.put("need_response", "true");
                                vector = pending_pref_sync;
                                synchronized (vector) {
                                    pending_pref_sync.addElement(p2.clone());
                                    SharedSessionReplicated.flushPendingPrefSync();
                                    ServerStatus.thisObj.server_info.put("replicated_servers_pending_pref_sync", String.valueOf(pending_pref_sync.size()));
                                }
                            }
                        } else if (action.startsWith("crushftp.handlers.")) {
                            if (ServerStatus.BG("replicate_users")) {
                                p2.put("need_response", "true");
                                vector = pending_user_sync;
                                synchronized (vector) {
                                    pending_user_sync.addElement(p2.clone());
                                    SharedSessionReplicated.flushPendingUserSync();
                                    ServerStatus.thisObj.server_info.put("replicated_servers_pending_user_sync", String.valueOf(pending_user_sync.size()));
                                }
                            }
                        } else if (action.equals("crushftp.AdminControls.renameJob") || action.equals("crushftp.AdminControls.removeJob") || action.equals("crushftp.AdminControls.addJob")) {
                            if (ServerStatus.BG("replicate_jobs")) {
                                p2.put("need_response", "true");
                                vector = pending_job_sync;
                                synchronized (vector) {
                                    pending_job_sync.addElement(p2.clone());
                                    SharedSessionReplicated.flushPendingJobSync();
                                    ServerStatus.thisObj.server_info.put("replicated_servers_pending_job_sync", String.valueOf(pending_job_sync.size()));
                                }
                            }
                        } else if (action.startsWith("crushftp.share.")) {
                            if (ServerStatus.BG("replicate_shares")) {
                                p2.put("need_response", "true");
                                vector = pending_share_sync;
                                synchronized (vector) {
                                    pending_share_sync.addElement(p2.clone());
                                    SharedSessionReplicated.flushPendingShareSync();
                                    ServerStatus.thisObj.server_info.put("replicated_servers_pending_share_sync", String.valueOf(pending_share_sync.size()));
                                }
                            }
                        } else {
                            Properties val2;
                            if (val != null && val instanceof Properties && ((val2 = (Properties)val).getProperty("need_response", "").equals("true") || p2.getProperty("need_response", "").equals("true"))) {
                                val2.put("time", String.valueOf(System.currentTimeMillis()));
                                pendingResponses.put(p2.getProperty("id"), val2);
                                ServerStatus.thisObj.server_info.put("replicated_servers_pendingResponses", String.valueOf(pendingResponses.size()));
                            }
                            Vector send_queue = (Vector)send_queues.get(remote_host_port);
                            p2.put("queued", String.valueOf(System.currentTimeMillis()));
                            send_queue.addElement(p2.clone());
                            ServerStatus.thisObj.server_info.put("replicated_servers_queue_" + xx, String.valueOf(send_queue.size()));
                        }
                    }
                }
                ++xx;
            }
            if (sync_delay > 3000L && System.currentTimeMillis() - lastActive < 10000L) {
                SharedSessionReplicated.flushNow();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.log("SERVER", 0, e);
        }
    }

    public static Vector runOnDmz(Properties job_tmp, String use_dmz, Vector tempItems, Vector log) throws Exception {
        String id = Common.makeBoundary(11);
        Log.log("SERVER", 0, "crushftp.JobScheduler.runJob:dmz_name=" + use_dmz + ":RUN:JOB:" + id);
        Properties request = new Properties();
        request.put("data", job_tmp);
        request.put("items", tempItems);
        DMZServerCommon.sendCommand(use_dmz, request, "RUN:JOB", id);
        Properties p = DMZServerCommon.getResponse(id, 600);
        String log_str = "" + p.remove("log");
        BufferedReader br = new BufferedReader(new StringReader(log_str));
        String data = "";
        while ((data = br.readLine()) != null) {
            log.addElement(String.valueOf(use_dmz) + ":" + data);
        }
        Properties result_info = (Properties)p.get("data");
        tempItems = (Vector)result_info.remove("newItems");
        Log.log("SERVER", 0, "crushftp.JobScheduler.runJob:dmz_name=" + use_dmz + ":RUN:JOB:" + id + ":complete");
        if (result_info.containsKey("errors")) {
            throw new Exception("" + result_info.get("errors"));
        }
        return tempItems;
    }

    public static void flushPendingPrefSync() {
        if (!enterprise_enabled) {
            return;
        }
        try {
            if (pending_pref_sync.size() == 0) {
                new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_pref_sync.xml").delete();
            } else if (ServerStatus.BG("replicate_preferences")) {
                Common.writeXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_pref_sync.xml", (Object)pending_pref_sync, "pending_pref_sync");
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
    }

    public static void flushPendingUserSync() {
        if (!enterprise_enabled) {
            return;
        }
        try {
            if (pending_user_sync.size() == 0) {
                new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_user_sync.xml").delete();
            } else if (ServerStatus.BG("replicate_users")) {
                Common.writeXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_user_sync.xml", (Object)pending_user_sync, "pending_user_sync");
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
    }

    public static void flushPendingJobSync() {
        if (!enterprise_enabled) {
            return;
        }
        try {
            if (pending_job_sync.size() == 0) {
                new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_job_sync.xml").delete();
            } else if (ServerStatus.BG("replicate_jobs")) {
                Common.writeXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_job_sync.xml", (Object)pending_job_sync, "pending_job_sync");
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
    }

    public static void flushPendingShareSync() {
        if (!enterprise_enabled) {
            return;
        }
        try {
            if (pending_share_sync.size() == 0) {
                new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_share_sync.xml").delete();
            } else if (ServerStatus.BG("replicate_shares")) {
                Common.writeXMLObject(String.valueOf(System.getProperty("crushftp.prefs")) + "cluster_share_sync.xml", (Object)pending_share_sync, "pending_share_sync");
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
    }
}

