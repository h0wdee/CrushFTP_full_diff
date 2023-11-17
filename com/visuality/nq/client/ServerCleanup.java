/*
 * Decompiled with CFR 0.152.
 */
package com.visuality.nq.client;

import com.visuality.nq.client.Server;
import com.visuality.nq.common.NqException;
import com.visuality.nq.common.TimeUtility;
import com.visuality.nq.common.TraceLog;
import com.visuality.nq.config.Config;

public class ServerCleanup
extends Thread {
    private static volatile boolean doExit = false;
    private static int wakeup;
    private static boolean isActive;
    private static ServerCleanup cleanupThread;
    private static Object waitObject;

    private ServerCleanup() {
        wakeup = (Integer)Config.jnq.getNE("CLEANUP_THREAD_SERVER_IDLE_PERIOD");
        boolean toEnable = (Boolean)Config.jnq.getNE("CLEANUP_THREAD_SERVER_ENABLED");
        if (toEnable) {
            this.setName("ServerCleanupThread");
            TraceLog.get().message("Starting ServerCleanupThread", 700);
            this.start();
            ServerCleanup.setIsActive(true);
        } else {
            TraceLog.get().message("ServerCleanupThread will not be started", 700);
        }
    }

    public static synchronized void initializeCleanupThread() {
        if (null == cleanupThread) {
            cleanupThread = new ServerCleanup();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static synchronized void terminate() {
        doExit = true;
        Object object = waitObject;
        synchronized (object) {
            if (ServerCleanup.isActive()) {
                waitObject.notify();
            }
        }
    }

    public static boolean isActive() {
        return isActive;
    }

    public static void setIsActive(boolean activeFlag) {
        isActive = activeFlag;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run() {
        ServerCleanup.setIsActive(true);
        while (!doExit) {
            try {
                Object object = waitObject;
                synchronized (object) {
                    waitObject.wait(TimeUtility.convertMinutesToMilliseconds(wakeup));
                }
                TraceLog.get().message("Woke up", 2000);
                this.cleanup();
            }
            catch (InterruptedException e) {
            }
            catch (NqException e) {
                TraceLog.get().error("Error during Server cleanup procedure = ", e);
            }
        }
        ServerCleanup.setIsActive(false);
    }

    private void cleanup() throws NqException {
        TraceLog.get().enter(700);
        Server.checkTimeouts();
        TraceLog.get().exit(700);
    }

    static {
        isActive = false;
        cleanupThread = null;
        waitObject = new Object();
    }
}

