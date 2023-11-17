/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import crushftp.handlers.SharedSession;
import crushftp.server.ServerStatus;

public class ShutdownHandler
extends Thread {
    boolean shutdown = false;

    public ShutdownHandler() {
        Runtime.getRuntime().addShutdownHook(this);
    }

    @Override
    public synchronized void run() {
        if (!this.shutdown) {
            SharedSession.shutdown();
            ServerStatus.thisObj.statTools.stopDB();
            ServerStatus.thisObj.searchTools.stopDB();
        }
        this.shutdown = true;
    }
}

