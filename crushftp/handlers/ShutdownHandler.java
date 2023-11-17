/*
 * Decompiled with CFR 0.152.
 */
package crushftp.handlers;

import crushftp.handlers.SharedSession;
import crushftp.server.AdminControls;
import crushftp.server.ServerStatus;
import java.util.Properties;

public class ShutdownHandler
extends Thread {
    boolean shutdown = false;

    public ShutdownHandler() {
        Runtime.getRuntime().addShutdownHook(this);
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public synchronized void run() {
        block6: {
            if (this.shutdown) break block6;
            AdminControls.stopLogins(new Properties(), "CONNECT)");
            start = System.currentTimeMillis();
            if (true) ** GOTO lbl13
            do {
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException var3_2) {
                    // empty catch block
                }
                if (ServerStatus.siVG("running_tasks").size() <= 0) break;
            } while (System.currentTimeMillis() - start < ServerStatus.LG("active_jobs_shutdown_wait_secs") * 1000L);
            SharedSession.shutdown();
            ServerStatus.thisObj.statTools.stopDB();
            ServerStatus.thisObj.searchTools.stopDB();
        }
        this.shutdown = true;
        if (ServerStatus.thisObj.loggingProvider1 != null) {
            ServerStatus.thisObj.loggingProvider1.flushNow();
        }
        if (ServerStatus.thisObj.loggingProvider2 != null) {
            ServerStatus.thisObj.loggingProvider2.flushNow();
        }
    }
}

