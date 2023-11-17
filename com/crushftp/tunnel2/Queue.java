/*
 * Decompiled with CFR 0.152.
 */
package com.crushftp.tunnel2;

import com.crushftp.tunnel2.Chunk;
import com.crushftp.tunnel2.DProperties;
import com.crushftp.tunnel2.Tunnel2;
import java.io.IOException;

public class Queue {
    public int id = 0;
    DProperties remote = null;
    boolean closedLocal = false;
    boolean closedRemote = false;
    Tunnel2 t = null;
    int remoteNum = 1;
    int localNum = 0;
    int max = -1;

    public Queue(Tunnel2 t, int id) {
        this.t = t;
        this.id = id;
        this.remote = new DProperties();
        t.addRemote(id, this.remote);
    }

    public void writeRemote(Chunk c) throws IOException {
        block17: {
            this.t.lastActivity = System.currentTimeMillis();
            if (c.isCommand()) {
                try {
                    String command = c.getCommand();
                    if (command.startsWith("A:")) break block17;
                    if (command.indexOf("PING") < 0) {
                        Tunnel2.msg("Tunnel2:" + command);
                    }
                    if (command.startsWith("PINGREADY:")) {
                        this.t.writeLocal(this.t.makeCommand(0, "PINGSEND:" + System.currentTimeMillis()), 0);
                    } else if (command.startsWith("PINGSEND:")) {
                        this.t.writeLocal(this.t.makeCommand(0, "PINGREPLY:" + command.split(":")[1]), 0);
                    } else if (command.startsWith("PINGREPLY:")) {
                        this.t.setPing((int)(System.currentTimeMillis() - Long.parseLong(command.split(":")[1].trim())));
                    } else if (command.startsWith("CLOSEIN:")) {
                        this.t.addWantClose();
                    } else if (command.startsWith("END:")) {
                        this.max = Integer.parseInt(command.split(":")[1].trim());
                    } else if (command.startsWith("CONNECT:")) {
                        this.t.connect(Integer.parseInt(command.split(":")[1]), command.split(":")[2], Integer.parseInt(command.split(":")[3]));
                    }
                }
                catch (Exception e) {
                    Tunnel2.msg(e);
                }
            } else {
                DProperties theRemote = this.t.getRemote(c.id);
                if (theRemote != null) {
                    theRemote.put(String.valueOf(c.num), c);
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public Chunk readRemote() throws IOException {
        DProperties dProperties = this.remote;
        synchronized (dProperties) {
            if (this.remote.containsKey(String.valueOf(this.remoteNum))) {
                ++this.remoteNum;
                return this.remote.remove(String.valueOf(this.remoteNum - 1));
            }
            return null;
        }
    }

    public synchronized void writeLocal(Chunk c, int i) throws IOException {
        if (c.num > this.localNum) {
            this.localNum = c.num;
        }
        this.t.writeLocal(c, i);
    }

    public synchronized void closeLocal() throws Exception {
        this.writeLocal(this.t.makeCommand(this.id, "END:" + this.localNum), -1);
        this.closedLocal = true;
    }

    public boolean isClosedLocal() {
        return this.closedLocal || !this.t.isActive();
    }

    public void closeRemote() {
        this.closedRemote = true;
    }

    public boolean isClosedRemote() {
        return this.closedRemote || !this.t.isActive();
    }

    public void waitForClose(int secs) throws Exception {
        int x = 0;
        while (x < secs) {
            Thread.sleep(1000L);
            if (this.isClosedRemote()) break;
            ++x;
        }
        if (!this.isClosedRemote()) {
            this.closeRemote();
        }
        this.remote.close();
    }
}

