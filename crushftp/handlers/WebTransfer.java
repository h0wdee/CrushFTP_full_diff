/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import crushftp.server.ServerStatus;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public class WebTransfer {
    Properties info = new Properties();
    Properties chunks = new Properties();
    Properties removedVals = new Properties();
    Properties transfer_info = new Properties();
    long pending_bytes = 0L;
    public static Object pending_bytes_lock = new Object();

    public WebTransfer(String direction, String session_id, String username) {
        this.transfer_info.put("time", "" + new Date());
        this.transfer_info.put("direction", direction);
        this.transfer_info.put("session_id", session_id);
        this.transfer_info.put("username", username);
    }

    public void setInfo(Properties request) {
        Enumeration<Object> keys = request.keys();
        while (keys.hasMoreElements()) {
            String key;
            String key2 = key = "" + keys.nextElement();
            if (key.startsWith("upload_")) {
                key2 = "transfer_" + key.substring(7);
            }
            if (key.startsWith("download_")) {
                key2 = "transfer_" + key.substring(7);
            }
            this.info.put(key2, request.getProperty(key));
        }
    }

    public void putObj(String key, Object val) {
        this.info.put(key, val);
    }

    public String getVal(String key) {
        return this.info.getProperty(key);
    }

    public String getVal(String key, String def) {
        return this.info.getProperty(key, def);
    }

    public Object getObj(String key) {
        return this.info.get(key);
    }

    public boolean hasObj(String key) {
        return this.info.containsKey(key);
    }

    public Object removeObj(String key) {
        Object o = this.info.remove(key);
        if (o instanceof String) {
            this.removedVals.put(key, o);
        }
        return o;
    }

    public void putAllObj(Properties obj) {
        this.info.putAll((Map<?, ?>)obj);
    }

    public void addChunk(String id, Object chunk) {
        this.chunks.put(id, chunk);
        if (chunk instanceof byte[]) {
            this.addBytes(((byte[])chunk).length);
        } else if (chunk instanceof Properties) {
            this.addBytes(((byte[])((Properties)chunk).get("b")).length);
        }
    }

    public Object getChunk(String id) {
        return this.chunks.get(id);
    }

    public Object removeChunk(String id) {
        Object chunk = this.chunks.remove(id);
        if (chunk instanceof byte[]) {
            this.removeBytes(((byte[])chunk).length);
        } else if (chunk instanceof Properties) {
            this.removeBytes(((byte[])((Properties)chunk).get("b")).length);
        }
        return chunk;
    }

    public boolean hasChunk(String id) {
        return this.chunks.containsKey(id);
    }

    public int getChunkCount() {
        return this.chunks.size();
    }

    public long getBytes() {
        return this.pending_bytes;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addBytes(long i) {
        Object object = pending_bytes_lock;
        synchronized (object) {
            this.pending_bytes += i;
            ServerStatus.siPUT("ram_pending_bytes", String.valueOf(ServerStatus.siLG("ram_pending_bytes") + i));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void removeBytes(long i) {
        Object object = pending_bytes_lock;
        synchronized (object) {
            this.pending_bytes -= i;
            ServerStatus.siPUT("ram_pending_bytes", String.valueOf(ServerStatus.siLG("ram_pending_bytes") - i));
        }
    }

    public void removeAllChunks() {
        Enumeration<Object> keys = this.chunks.keys();
        while (keys.hasMoreElements()) {
            this.removeChunk("" + keys.nextElement());
        }
    }
}

