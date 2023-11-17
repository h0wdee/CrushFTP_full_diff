/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import com.crushftp.client.Worker;
import crushftp.handlers.Log;
import crushftp.handlers.SessionCrush;
import java.net.Socket;
import java.util.Date;
import java.util.Vector;

public class IdleMonitor {
    public static Vector children = new Vector();
    public int sleep_interval = 10000;
    public long last_activity = 0L;
    public long timeout = 0L;
    Thread the_thread = null;
    public SessionCrush calling_session = null;
    public boolean enabled = true;
    public boolean die_now = false;
    Socket sock = null;
    String type = "";

    public IdleMonitor(SessionCrush calling_session, long last_activity, long timeout, Thread the_thread) {
        this.calling_session = calling_session;
        this.the_thread = the_thread;
        this.last_activity = last_activity;
        this.timeout = timeout;
        children.addElement(this);
    }

    public IdleMonitor(SessionCrush calling_session, long last_activity, long timeout, Thread the_thread, Socket sock) {
        this.calling_session = calling_session;
        this.the_thread = the_thread;
        this.last_activity = last_activity;
        this.timeout = timeout;
        this.sock = sock;
        children.addElement(this);
    }

    public boolean finished() {
        boolean exit = this.die_now;
        try {
            long timeout2;
            if (this.timeout < 0L) {
                exit = true;
            }
            if (this.die_now || this.calling_session != null && this.calling_session.uiBG("dieing") || this.calling_session != null && this.calling_session.session_socks.size() == 0) {
                exit = true;
            }
            timeout2 = (timeout2 = this.timeout) < 0L ? (timeout2 *= -1L) : (timeout2 *= 60L);
            Thread.currentThread().setName("Global Idle Monitor:" + children.size() + ":" + timeout2 + ":" + (new Date().getTime() - this.last_activity) + ":" + exit + ":" + this.timeout);
            if (new Date().getTime() - this.last_activity > timeout2 * 1000L && this.enabled) {
                exit = true;
                Worker.startWorker(new Runnable(){

                    @Override
                    public void run() {
                        try {
                            if (IdleMonitor.this.calling_session != null) {
                                IdleMonitor.this.calling_session.uiPUT("termination_message", "TIMEOUT");
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        try {
                            if (IdleMonitor.this.calling_session != null) {
                                IdleMonitor.this.calling_session.not_done = false;
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        try {
                            if (IdleMonitor.this.the_thread != null) {
                                IdleMonitor.this.the_thread.interrupt();
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                        try {
                            if (IdleMonitor.this.calling_session != null) {
                                Log.log("SERVER", 1, "Closing idle session:" + Thread.currentThread().getName());
                                IdleMonitor.this.calling_session.do_kill(null);
                            } else if (IdleMonitor.this.sock != null) {
                                Log.log("SERVER", 1, "Closing idle HTTP socket before login:" + IdleMonitor.this.sock);
                                IdleMonitor.this.sock.close();
                            }
                        }
                        catch (Exception exception) {
                            // empty catch block
                        }
                    }
                }, "IdleMonitor closer");
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return exit;
    }

    public static void init() throws Exception {
        Worker.startWorker(new Runnable(){

            @Override
            public void run() {
                while (true) {
                    Thread.currentThread().setName("Global Idle Monitor:" + children.size());
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException interruptedException) {
                        // empty catch block
                    }
                    int x = children.size() - 1;
                    while (x >= 0) {
                        if (((IdleMonitor)children.elementAt(x)).finished()) {
                            children.remove(x);
                        }
                        --x;
                    }
                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException interruptedException) {
                        continue;
                    }
                    break;
                }
            }
        }, "Global Idle Monitor");
    }
}

