/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.job;

import com.crushftp.client.Common;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Map;
import java.util.Properties;

public class JobReference
extends Properties {
    String host = null;
    int port = 0;

    public JobReference(Properties tracker, String host, int port) {
        super.putAll((Map<?, ?>)tracker);
        this.host = host;
        this.port = port;
    }

    @Override
    public Object put(Object key, Object val) {
        this.putRemote(key, val);
        return super.put(key, val);
    }

    public void putRemote(Object key, Object val) {
        Socket sock = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            try {
                sock = new Socket(this.host, this.port);
                oos = new ObjectOutputStream(sock.getOutputStream());
                ois = new ObjectInputStream(sock.getInputStream());
                Properties p = new Properties();
                p.put("action", "putRemote");
                p.put("tracker_id", super.get("id"));
                p.put("key", key);
                if (val != null) {
                    p.put("val", val);
                }
                oos.writeUnshared(p);
                oos.flush();
            }
            catch (Exception e) {
                Common.log("SERVER", 0, "JobBroker JobReference putRemote error!:" + e);
                try {
                    if (ois != null) {
                        ois.close();
                    }
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                try {
                    if (oos != null) {
                        oos.close();
                    }
                }
                catch (IOException iOException) {
                    // empty catch block
                }
                try {
                    if (sock != null) {
                        sock.close();
                    }
                }
                catch (IOException iOException) {}
            }
        }
        finally {
            try {
                if (ois != null) {
                    ois.close();
                }
            }
            catch (IOException iOException) {}
            try {
                if (oos != null) {
                    oos.close();
                }
            }
            catch (IOException iOException) {}
            try {
                if (sock != null) {
                    sock.close();
                }
            }
            catch (IOException iOException) {}
        }
    }
}

