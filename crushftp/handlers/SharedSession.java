/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.File_S;
import com.crushftp.client.Worker;
import crushftp.handlers.Common;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import crushftp.handlers.SharedSessionReplicated;
import crushftp.server.QuickConnect;
import crushftp.server.ServerStatus;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

public class SharedSession {
    public static transient Object sessionLock = new Object();
    public static transient Object sessionFindLock = new Object();
    private static Properties thisObj = null;
    String id = "";
    static boolean shutting_down = false;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static void init() {
        if (thisObj != null) return;
        Object object = sessionLock;
        synchronized (object) {
            if (thisObj != null) return;
            ObjectInputStream ois = null;
            try {
                if (new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/lib/sessionCache.xml").exists()) {
                    Common.recurseDelete(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/cache/", false);
                    new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/lib/sessionCache.xml").delete();
                    new File_S(String.valueOf(System.getProperty("crushftp.plugins")) + "plugins/lib/ehcache-core-2.5.0.jar").delete();
                }
                if (new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions.obj").exists()) {
                    SharedSession recent_users;
                    Object o;
                    ois = new ObjectInputStream(new FileInputStream(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions.obj")));
                    thisObj = (Properties)ois.readObject();
                    thisObj.remove("crushftp.usernames.activity");
                    SharedSession user_sessions = SharedSession.find("crushftp.sessions");
                    Enumeration keys = user_sessions.keys();
                    if (keys.hasMoreElements()) {
                        String key = keys.nextElement().toString();
                        o = user_sessions.get(key);
                        if (o instanceof Properties) {
                            user_sessions.remove(key);
                        } else if (o instanceof SessionCrush && ((SessionCrush)o).getProperty("last_activity") == null) {
                            ((SessionCrush)o).active();
                        }
                    }
                    if ((recent_users = SharedSession.find("recent_user_list")).get("recent_user_list") != null) {
                        ServerStatus.siVG("recent_user_list").addAll((Vector)recent_users.get("recent_user_list"));
                        o = QuickConnect.syncUserNumbers;
                        synchronized (o) {
                            int maxNum = 0;
                            int x = 0;
                            while (true) {
                                if (x >= ServerStatus.siVG("recent_user_list").size()) {
                                    int saved_maxNum = Integer.parseInt(recent_users.getProperty("user_login_num", "0"));
                                    if (saved_maxNum > maxNum) {
                                        maxNum = saved_maxNum;
                                    }
                                    ServerStatus.put_in("user_login_num", String.valueOf(++maxNum));
                                    break;
                                }
                                Properties p = (Properties)ServerStatus.siVG("recent_user_list").elementAt(x);
                                int userNum = Integer.parseInt(p.getProperty("user_number", "0"));
                                if (userNum > maxNum) {
                                    maxNum = userNum;
                                }
                                ++x;
                            }
                        }
                    }
                    SharedSession reresh_tokens = SharedSession.find("crushftp.refresh_tokens");
                    com.crushftp.client.Common.initRefreshTokens(SharedSession.getCache("crushftp.refresh_tokens"));
                    ois.close();
                    ois = null;
                }
                if (new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "md4_hashes.obj").exists()) {
                    ois = new ObjectInputStream(new FileInputStream(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "md4_hashes.obj")));
                    Properties md4_hashes = (Properties)ois.readObject();
                    ServerStatus.thisObj.server_info.put("md4_hashes", md4_hashes);
                    ois.close();
                    ois = null;
                }
            }
            catch (Throwable e) {
                Log.log("SERVER", 0, e);
                try {
                    if (ois != null) {
                        ois.close();
                    }
                }
                catch (Exception exception) {
                    // empty catch block
                }
                new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions.obj").delete();
            }
            if (thisObj == null) {
                thisObj = new Properties();
            }
            try {
                SharedSessionReplicated.init();
            }
            catch (Exception e) {
                Log.log("SERVER", 0, e);
            }
            return;
        }
    }

    /*
     * Enabled aggressive block sorting
     */
    public static void shutdown() {
        if (ServerStatus.BG("allow_session_caching_on_exit")) {
            while (ServerStatus.siVG("user_list").size() > 0) {
                Properties user_info = (Properties)ServerStatus.siVG("user_list").elementAt(0);
                SessionCrush thisSession = (SessionCrush)user_info.get("session");
                if (thisSession != null) {
                    thisSession.do_kill(null);
                } else {
                    ServerStatus.thisObj.remove_user(user_info);
                }
                ServerStatus.siVG("user_list").remove(user_info);
            }
            SharedSession recent_users = SharedSession.find("recent_user_list");
            recent_users.put("recent_user_list", ServerStatus.siVG("recent_user_list"));
            recent_users.put("user_login_num", ServerStatus.siSG("user_login_num"));
        }
        shutting_down = true;
        SharedSession.flush();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void flush() {
        final Thread currThread = Thread.currentThread();
        final StringBuffer status = new StringBuffer();
        try {
            new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions2.obj").delete();
            ObjectOutputStream oos1 = null;
            final ByteArrayOutputStream baos = new ByteArrayOutputStream();
            oos1 = ServerStatus.BG("allow_session_caching_memory") ? new ObjectOutputStream(baos) : new ObjectOutputStream(new FileOutputStream(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions2.obj")));
            final ObjectOutputStream oos2 = oos1;
            Runnable r = new Runnable(){

                @Override
                public void run() {
                    int x = 0;
                    while (x < 120 && status.length() == 0) {
                        try {
                            Thread.sleep(1000L);
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        ++x;
                    }
                    if (status.length() == 0) {
                        Log.log("SERVER", 0, "TIMEOUT waiting for sessions flush...");
                        com.crushftp.client.Common.dumpStack("TIMEOUT waiting for sessions flush...");
                        currThread.interrupt();
                    }
                    try {
                        oos2.close();
                    }
                    catch (Exception exception) {
                        // empty catch block
                    }
                }
            };
            Worker.startWorker(r);
            SharedSession.find("running_tasks").remove("running_tasks");
            Object object = sessionLock;
            synchronized (object) {
                Object object2 = sessionFindLock;
                synchronized (object2) {
                    try {
                        oos2.writeObject(thisObj);
                        oos2.flush();
                        oos2.close();
                        if (!ServerStatus.BG("allow_session_caching_memory")) {
                            new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions.obj").delete();
                            new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions2.obj").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions.obj"));
                        }
                    }
                    finally {
                        try {
                            oos2.close();
                        }
                        catch (Exception exception) {}
                    }
                }
            }
            if (ServerStatus.BG("allow_session_caching_memory")) {
                r = new Runnable(){

                    @Override
                    public void run() {
                        try {
                            FileOutputStream out = new FileOutputStream(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions2.obj"));
                            out.write(baos.toByteArray());
                            out.close();
                            new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions.obj").delete();
                            new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions2.obj").renameTo(new File_S(String.valueOf(System.getProperty("crushftp.prefs")) + "sessions.obj"));
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                };
                if (shutting_down) {
                    r.run();
                } else {
                    Worker.startWorker(r);
                }
            }
        }
        catch (Exception e) {
            Log.log("SERVER", 0, e);
        }
        status.append("done");
    }

    public static void findBadObjects2(Object o) {
        SharedSession.findBadObjects2(o, "/", 0);
    }

    /*
     * Unable to fully structure code
     */
    public static void findBadObjects2(Object o, String path, int depth) {
        block9: {
            block8: {
                if (depth > 50) {
                    return;
                }
                try {
                    baos = new ByteArrayOutputStream();
                    oos = new ObjectOutputStream(baos);
                    oos.writeObject(o);
                    oos.close();
                    return;
                }
                catch (Exception e) {
                    Log.log("SERVER", 0, String.valueOf(path) + ":" + o);
                    System.out.println(new Date() + ":" + path + ":" + o.getClass().toString());
                    if (!(o instanceof Properties)) break block8;
                    p = (Properties)o;
                    keys = p.keys();
                    ** while (keys.hasMoreElements())
                }
lbl-1000:
                // 1 sources

                {
                    key = keys.nextElement();
                    SharedSession.findBadObjects2(p.get(key), String.valueOf(path) + key + "/", depth + 1);
                    continue;
lbl20:
                    // 1 sources

                    break block9;
                }
            }
            if (o instanceof Vector) {
                v = (Vector)o;
                v = (Vector)v.clone();
                x = 0;
                while (x < v.size()) {
                    SharedSession.findBadObjects2(v.elementAt(x), String.valueOf(path) + "[" + x + "]/", depth + 1);
                    ++x;
                }
            } else if (!(o instanceof String)) {
                o instanceof SessionCrush;
            }
        }
    }

    private static boolean isShared(String key) {
        return SharedSessionReplicated.replicatedItems.indexOf(key.toString()) >= 0;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static SharedSession find(String id) {
        SharedSession.init();
        Object object = sessionFindLock;
        synchronized (object) {
            if (SharedSession.isShared(id)) {
                if (!thisObj.containsKey(id)) {
                    thisObj.put(id, new Properties());
                }
            } else if (!thisObj.containsKey(id)) {
                thisObj.put(id, new Properties());
            }
            return new SharedSession(id);
        }
    }

    private SharedSession(String id) {
        this.id = id;
    }

    public void put(Object key, Object val) {
        this.put(key, val, true);
    }

    public void put(Object key, Object val, boolean replicate) {
        if (replicate && SharedSession.isShared(this.id) && ServerStatus.BG("replicate_sessions9")) {
            SharedSessionReplicated.send(this.id, "put", key, val);
        }
        Properties cache = (Properties)thisObj.get(this.id);
        if (val != null) {
            cache.put(key, val);
        }
    }

    public static Properties getCache(String id) {
        return (Properties)thisObj.get(id);
    }

    public Object get(Object key) {
        Properties cache = (Properties)thisObj.get(this.id);
        return cache.get(key);
    }

    public String getProperty(String key, String val) {
        Properties cache = (Properties)thisObj.get(this.id);
        return cache.getProperty(key, val);
    }

    public String getProperty(String key) {
        Properties cache = (Properties)thisObj.get(this.id);
        return cache.getProperty(key);
    }

    public Object remove(Object key) {
        return this.remove(key, true);
    }

    public Object remove(Object key, boolean replicate) {
        if (replicate && SharedSession.isShared(this.id) && ServerStatus.BG("replicate_sessions9")) {
            SharedSessionReplicated.send(this.id, "remove", key, null);
        }
        Properties cache = (Properties)thisObj.get(this.id);
        return cache.remove(key);
    }

    public boolean containsKey(String key) {
        Properties cache = (Properties)thisObj.get(this.id);
        return cache.containsKey(key);
    }

    public Enumeration keys() {
        Properties cache = (Properties)thisObj.get(this.id);
        return cache.keys();
    }
}

